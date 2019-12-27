package com.wezom.kiviremoteserver.service.aspect.aspect_v2

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.SystemClock
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.wezom.kiviremoteserver.R
import com.wezom.kiviremoteserver.environment.EnvironmentPictureSettings
import com.wezom.kiviremoteserver.service.aspect.Alarm
import com.wezom.kiviremoteserver.service.aspect.aspect_v2.data.CardData
import com.wezom.kiviremoteserver.service.aspect.aspect_v2.data.Cards
import com.wezom.kiviremoteserver.service.aspect.items.TimerValues
import com.wezom.kiviremoteserver.ui.views.pageindicatorview.PageIndicatorView

//val onChangeModeListener: (Int, IFLMItems) -> Unit

class CardsMainAdapter(var items: List<CardData>, val action1: (data: CardData) -> Unit, val action2: () -> Unit, val pictureSettings: EnvironmentPictureSettings) : RecyclerView.Adapter<CardsMainAdapter.CardMainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardMainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.menu_item, parent, false)
        val holder = CardMainViewHolder(view)
        holder.card.setOnFocusChangeListener { _, hasFocus -> holder.scaleCard(hasFocus) }
        return holder
    }

    override fun onBindViewHolder(holder: CardMainViewHolder, position: Int) {
        if (position == 0) {
            holder.card.requestFocus()
        }
        holder.bindData(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class CardMainViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView = view.findViewById(R.id.card)
        private val actionsView: View = view.findViewById(R.id.actions_view)
        private val imageView: ImageView = view.findViewById(R.id.imageView)
        private val mainText: TextView = view.findViewById(R.id.mainText)
        private val secondText: TextView = view.findViewById(R.id.secondText)
        private val progressIndicator: PageIndicatorView = view.findViewById(R.id.progressIndicator)
        private var indicatorCurrentIndex = 0
        private var showActionsView = false

        fun bindData(data: CardData) {
            mainText.setText(data.title)
            imageView.setImageResource(data.image)
//            showActionsView = data.values.isNotEmpty()
//            if (showActionsView) {
            showActionsView = data.showActionsView

            card.setOnKeyListener { _, _, event ->
                return@setOnKeyListener when {
                    event.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_DPAD_UP -> {
                        if (showActionsView) {

                            val indexOfUserMode = data.values.indexOfFirst { it.stringRes == R.string.user || it.stringRes == R.string.sound_user }
                            if (indexOfUserMode < 0 || indexOfUserMode == indicatorCurrentIndex) {
                                action1(data)
                                return@setOnKeyListener true
                            }

                            indicatorCurrentIndex = indexOfUserMode
                            progressIndicator.selection = indexOfUserMode % data.values.size
                            secondText.setText(data.values[indexOfUserMode % data.values.size].stringRes)

                            when (data.type) {
                                2 -> {
                                    pictureSettings.pictureMode = data.values[indexOfUserMode % data.values.size].id
                                    pictureSettings.initSettings(itemView.context)
                                }
                                5 -> {
                                    pictureSettings.soundType = data.values[indexOfUserMode % data.values.size].id
                                }
                            }

                            action1(data)
                        }

                        true
                    }
                    event.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_BACK -> {
                        action2()
                        true
                    }
                    else -> event.keyCode == KeyEvent.KEYCODE_DPAD_UP || event.keyCode == KeyEvent.KEYCODE_BACK
                }
            }
//            }

            actionsView.visibility = if (card.isFocused && showActionsView) View.VISIBLE else View.INVISIBLE

            if (data.values.isNotEmpty()) {

                indicatorCurrentIndex = when (data.type) {
                    2 -> data.values.indexOfFirst { it.id == pictureSettings.pictureMode }
                    5 -> data.values.indexOfFirst { it.id == pictureSettings.soundType }
                    6 -> {
                        val slipIn = Settings.Global.getLong(itemView.context.contentResolver, "sleep_timer_remain", SystemClock.elapsedRealtime())
                        if (slipIn <= SystemClock.elapsedRealtime()) { 0 }
                        val timeToSleep = (slipIn - SystemClock.elapsedRealtime()).toInt() / 1000
                        val minutesLeft = timeToSleep / 60

                        Cards.allData[4].values.indexOfFirst { (it as TimerValues).minutes >= minutesLeft }
                    }
                    3 -> data.values.indexOfFirst { it.id == pictureSettings.videoArcType }
                    else -> 0
                }

                if (indicatorCurrentIndex < 0) {
                    indicatorCurrentIndex = 0
                }

                secondText.setText(data.values[indicatorCurrentIndex].stringRes)
                progressIndicator.visibility = View.VISIBLE
                progressIndicator.count = data.values.size
                progressIndicator.selection = indicatorCurrentIndex

                card.setOnClickListener {
                    indicatorCurrentIndex++
                    progressIndicator.selection = indicatorCurrentIndex % data.values.size
                    secondText.setText(data.values[indicatorCurrentIndex % data.values.size].stringRes)

                    when (data.type) {
                        2 -> {
                            pictureSettings.pictureMode = data.values[indicatorCurrentIndex % data.values.size].id
                            pictureSettings.initSettings(itemView.context)
                        }
                        5 -> {
                            pictureSettings.soundType = data.values[indicatorCurrentIndex % data.values.size].id
                        }
                        6 -> {
                            PreferenceManager.getDefaultSharedPreferences(itemView.context).edit().remove("shutDownTime").commit()
                            val index = indicatorCurrentIndex % data.values.size
                            setAlarm(index, itemView.context)
                        }
                        3 -> {
                            pictureSettings.videoArcType = data.values[indicatorCurrentIndex % data.values.size].id
                        }
                    }
                }
            } else {
                secondText.setText(data.subTitle)
                card.setOnClickListener(null)
                progressIndicator.visibility = View.GONE
            }
        }

        fun scaleCard(hasFocus: Boolean) {
            val color = Color.parseColor(if (hasFocus) "#444444" else "#333333")
            card.setCardBackgroundColor(color)
            card.animate()
                    .scaleY(if (hasFocus) 1.1f else 1f)
                    .scaleX(if (hasFocus) 1.1f else 1f)
                    .setDuration(100)
                    .setInterpolator(FastOutSlowInInterpolator())
                    .start()

            actionsView.visibility = if (hasFocus && showActionsView) View.VISIBLE else View.INVISIBLE
            itemView.translationZ = (if (hasFocus) 5 else 0).toFloat()
        }
    }

    fun setAlarm(alarmPosition: Int, context: Context) {
        val minutesLeft = (Cards.allData[4].values[alarmPosition] as TimerValues).minutes

        val intent = Intent(context, Alarm::class.java)
        val pi = PendingIntent.getService(context, 0, intent, 0)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && minutesLeft > 0) {
            Log.e("Timer", "Millis: ${minutesLeft * 60 * 1000L}")
            alarmManager.cancel(pi)
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + minutesLeft * 60 * 1000, pi)
        }

        if (minutesLeft > 0) {
            val slipIn = SystemClock.elapsedRealtime() + minutesLeft * 60 * 1000
            PreferenceManager.getDefaultSharedPreferences(context).edit().putLong("shutDownTime", slipIn).commit()
            sleepIn(minutesLeft * 60 * 1000, context)
        } else {
            sleepIn(0, context)
        }
    }

    private fun sleepIn(delay: Int, context: Context) {
        Settings.Global.putLong(context.contentResolver, "sleep_timer_remain", SystemClock.elapsedRealtime() + delay)
        Settings.Global.putInt(context.contentResolver, "sleep_timer", delay / (60 * 1000))
        val intentForToast = Intent()
        intentForToast.component = ComponentName("com.hikeen.menu", "com.hikeen.menu.util.SleepTimeRecevier")
        intentForToast.action = "com.kivi.sleep.action"
        context.sendBroadcast(intentForToast)
    }

}