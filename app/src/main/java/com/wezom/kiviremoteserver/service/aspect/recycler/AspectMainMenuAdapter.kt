package com.wezom.kiviremoteserver.service.aspect.recycler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.content.ContextCompat
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
import com.wezom.kiviremoteserver.service.aspect.data.AspectMenuItem
import com.wezom.kiviremoteserver.service.aspect.data.AspectMenuItem.Companion.TYPE_KEYBOARD
import com.wezom.kiviremoteserver.service.aspect.data.AspectMenuItem.Companion.TYPE_PICTURE
import com.wezom.kiviremoteserver.service.aspect.data.AspectMenuItem.Companion.TYPE_RATIO
import com.wezom.kiviremoteserver.service.aspect.data.AspectMenuItem.Companion.TYPE_SETTINGS
import com.wezom.kiviremoteserver.service.aspect.data.AspectMenuItem.Companion.TYPE_SOUND
import com.wezom.kiviremoteserver.service.aspect.data.AspectMenuItem.Companion.TYPE_TIMER
import com.wezom.kiviremoteserver.service.aspect.data.AspectMenuItems
import com.wezom.kiviremoteserver.service.aspect.values.TimerValues
import com.wezom.kiviremoteserver.ui.views.pageindicatorview.PageIndicatorView

class AspectMainMenuAdapter(var items: List<AspectMenuItem>, val pictureSettings: EnvironmentPictureSettings,
                            val onDpadUpClick: (data: AspectMenuItem) -> Unit,
                            val onBackClick: () -> Unit,
                            val onSettingsClick: () -> Unit,
                            val onKeyboardClick: () -> Unit) : RecyclerView.Adapter<AspectMainMenuAdapter.CardMainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardMainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.menu_item, parent, false)
        val holder = CardMainViewHolder(view)
        holder.card.setOnFocusChangeListener { _, hasFocus -> holder.scaleCard(hasFocus) }
        return holder
    }

    override fun onBindViewHolder(holder: CardMainViewHolder, position: Int) {
        if (position == 0) {
            holder.card.requestFocus()
            holder.card.nextFocusLeftId = holder.card.id
        }

        if (position == items.size - 1) {
            holder.card.nextFocusRightId = holder.card.id
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

        fun bindData(data: AspectMenuItem) {
            mainText.setText(data.title)
            imageView.setImageResource(data.image)
            showActionsView = data.showActionsView

            card.setOnKeyListener { _, _, event ->
                return@setOnKeyListener when {
                    event.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_DPAD_UP -> {
                        if (showActionsView) {
                            val indexOfUserMode = data.values.indexOfFirst { it.stringRes == R.string.user || it.stringRes == R.string.sound_user }
                            if (indexOfUserMode < 0 || indexOfUserMode == indicatorCurrentIndex) {
                                onDpadUpClick(data)
                                return@setOnKeyListener true
                            }

                            indicatorCurrentIndex = indexOfUserMode
                            progressIndicator.selection = indexOfUserMode % data.values.size
                            secondText.setText(data.values[indexOfUserMode % data.values.size].stringRes)

                            when (data.type) {
                                TYPE_PICTURE -> {
                                    pictureSettings.pictureMode = data.values[indexOfUserMode % data.values.size].id
                                    pictureSettings.initSettings(itemView.context)
                                }

                                TYPE_SOUND -> {
                                    pictureSettings.soundType = data.values[indexOfUserMode % data.values.size].id
                                }
                            }

                            onDpadUpClick(data)
                        }

                        true
                    }

                    event.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_BACK -> {
                        onBackClick()
                        true
                    }

                    else -> event.keyCode == KeyEvent.KEYCODE_DPAD_UP || event.keyCode == KeyEvent.KEYCODE_BACK
                }
            }

            actionsView.visibility = if (card.isFocused && showActionsView) View.VISIBLE else View.INVISIBLE

            if (data.values.isNotEmpty()) {
                indicatorCurrentIndex = when (data.type) {
                    TYPE_PICTURE -> data.values.indexOfFirst { it.id == pictureSettings.pictureMode }
                    TYPE_SOUND -> data.values.indexOfFirst { it.id == pictureSettings.soundType }
                    TYPE_RATIO -> data.values.indexOfFirst { it.id == pictureSettings.videoArcType }
                    TYPE_TIMER -> {
                        val slipIn = Settings.Global.getLong(itemView.context.contentResolver, "sleep_timer_remain", SystemClock.elapsedRealtime())
                        if (slipIn <= SystemClock.elapsedRealtime()) { 0 }
                        val timeToSleep = (slipIn - SystemClock.elapsedRealtime()).toInt() / 1000
                        val minutesLeft = timeToSleep / 60

                        AspectMenuItems.allData[TYPE_TIMER]?.values?.indexOfFirst { (it as TimerValues).minutes >= minutesLeft } ?: 0
                    }
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
                        TYPE_PICTURE -> {
                            pictureSettings.pictureMode = data.values[indicatorCurrentIndex % data.values.size].id
                            pictureSettings.initSettings(itemView.context)
                        }
                        TYPE_SOUND -> {
                            pictureSettings.soundType = data.values[indicatorCurrentIndex % data.values.size].id
                        }
                        TYPE_TIMER -> {
                            PreferenceManager.getDefaultSharedPreferences(itemView.context).edit().remove("shutDownTime").apply()
                            val index = indicatorCurrentIndex % data.values.size
                            setAlarm(index, itemView.context)
                        }
                        TYPE_RATIO -> {
                            pictureSettings.videoArcType = data.values[indicatorCurrentIndex % data.values.size].id
                        }
                    }
                }
            } else {
                secondText.setText(data.subTitle)
                if (data.type == TYPE_SETTINGS) {
                    card.setOnClickListener { onSettingsClick() }
                } else if (data.type == TYPE_KEYBOARD) {
                    card.setOnClickListener { onKeyboardClick() }
                } else {
                    card.setOnClickListener(null)
                }

                progressIndicator.visibility = View.GONE
            }
        }

        fun scaleCard(hasFocus: Boolean) {
            val color = ContextCompat.getColor(itemView.context, if (hasFocus) R.color.tundora else R.color.mine_shaft)
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
        val minutesLeft = (AspectMenuItems.allData[TYPE_TIMER]?.values?.get(alarmPosition) as TimerValues).minutes

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
            PreferenceManager.getDefaultSharedPreferences(context).edit().putLong("shutDownTime", slipIn).apply()
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