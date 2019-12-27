package com.wezom.kiviremoteserver.service.aspect

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.*
import com.wezom.kiviremoteserver.App
import com.wezom.kiviremoteserver.R
import com.wezom.kiviremoteserver.environment.EnvironmentInputsHelper
import com.wezom.kiviremoteserver.environment.EnvironmentPictureSettings
import com.wezom.kiviremoteserver.service.aspect.aspect_v2.CardsMainAdapter
import com.wezom.kiviremoteserver.service.aspect.aspect_v2.InputsAdapter
import com.wezom.kiviremoteserver.service.aspect.aspect_v2.data.CardData
import com.wezom.kiviremoteserver.service.aspect.aspect_v2.data.Cards
import wezom.kiviremoteserver.environment.bridge.BridgePicture

class AspectLayoutService_NEW : Service() {

    private lateinit var pbValue: TextView
    private lateinit var pb: ProgressBar
    private lateinit var textPicker: NumberPicker
    private lateinit var windowManager: WindowManager

    private val pictureSettings: EnvironmentPictureSettings = EnvironmentPictureSettings()
    private val inputsHelper: EnvironmentInputsHelper = EnvironmentInputsHelper()

    private var autoCloseTime = 10
    private val timer = Handler()

    private val generalType = BridgePicture.LAYER_TYPE//WindowManager.LayoutParams.TYPE_TOAST;
    private lateinit var generalView: RelativeLayout
    private lateinit var mainMenu: RecyclerView
    private lateinit var inputsList: RecyclerView
    private lateinit var inputsListContainer: CardView
    private lateinit var secondaryMenu: LinearLayout
    private lateinit var backView: View

    private val updateSleepTime = object : Runnable {
        override fun run() {
            timer.postDelayed(this, 1000)
            if (autoCloseTime > 0 && System.currentTimeMillis() - autoCloseTime > lastUpdate)  {
                stopSelf()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && App.checkWizard(this)) {
            return
        }

        autoCloseTime = Settings.Global.getInt(contentResolver, OSD_TIME, 10)
        if (autoCloseTime in 1..9) { autoCloseTime = 10 }

        //TODO remove autoCloseTime *= 10;
        autoCloseTime *= 1000000
        autoCloseTime *= 1000
        mainColor = resources.getColor(R.color.colorPrimary)

        lastUpdate = System.currentTimeMillis()
        initLayout(baseContext)

        timer.postDelayed(updateSleepTime, autoCloseTime.toLong())
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun initLayout(context: Context) {
        windowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        generalView = View.inflate(context, R.layout.layout_aspect_v2, null) as RelativeLayout
        val param = WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                generalType, //TYPE_SYSTEM_ALERT
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT)
        windowManager.addView(generalView, param)
        generalView.visibility = View.VISIBLE
        generalView.clearAnimation()

        secondaryMenu = generalView.findViewById(R.id.secondary_menu)
        textPicker = generalView.findViewById(R.id.text_picker)
        pb = generalView.findViewById(R.id.pb)
        pbValue = generalView.findViewById(R.id.tv_value)
        backView = generalView.findViewById(R.id.back_view)

        initMainCards()
        initInputs()

        mainMenu.animate()
                .translationY(0f)
                .setDuration(400)
                .setInterpolator(DecelerateInterpolator())
                .start()

        val animFadeIn = AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_in)
        animFadeIn.duration = 600
        animFadeIn.interpolator = DecelerateInterpolator()
        backView.startAnimation(animFadeIn)
    }

    private fun initMainCards() {
        mainMenu = generalView.findViewById(R.id.main_menu)
        mainMenu.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapter = CardsMainAdapter(Cards.allData, { data -> setModeUp(true, data) }, {
            if (isStartedClosing) { return@CardsMainAdapter }

            isStartedClosing = true
            mainMenu.animate()/*.alpha(isTop ? 0.9f : 1)*/
                    .translationY(500f)
                    .setDuration(400)
                    .setInterpolator(DecelerateInterpolator())
                    .start()

            val animFadeOut = AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_out)
            animFadeOut.duration = 600
            animFadeOut.interpolator = DecelerateInterpolator()
            animFadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) { stopSelf() }
            })

            backView.startAnimation(animFadeOut)
        }, pictureSettings)
        mainMenu.adapter = adapter
    }

    private fun initInputs() {
        inputsListContainer = generalView.findViewById(R.id.cv_inputs)
        inputsList = generalView.findViewById(R.id.rv_inputs)
        inputsList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapter = InputsAdapter(inputsHelper, {
            //TODO show backView
            setModeUp(false)

            val animFadeIn = AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_in)
            animFadeIn.duration = 600
            animFadeIn.interpolator = DecelerateInterpolator()
            animFadeIn.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) { backView.visibility = View.VISIBLE }
            })

            backView.startAnimation(animFadeIn)
        }, inputsListContainer.context)
        inputsList.adapter = adapter
    }

    var lastFocusChildIndex = 0

    private fun setModeUp(isTop: Boolean, data: CardData? = null) {
        if (isTop) {
            lastFocusChildIndex = mainMenu.indexOfChild(mainMenu.focusedChild)

            when (data?.type) {
                2 -> {
                    presentPictureModeSettings()
                    secondaryMenu.findViewById<NumberPicker>(R.id.text_picker).requestFocus()

                    secondaryMenu.animate()
                        .alpha(1.toFloat())
                        .translationY((-50).toFloat())
                        .setInterpolator(DecelerateInterpolator())
                        .setDuration(400)
                        .start()
                }
                5 -> {
                    presentSoundModeSettings()
                    secondaryMenu.findViewById<NumberPicker>(R.id.text_picker).requestFocus()

                    secondaryMenu.animate()
                            .alpha(1.toFloat())
                            .translationY((-50).toFloat())
                            .setInterpolator(DecelerateInterpolator())
                            .setDuration(400)
                            .start()
                }
                0 -> {
                    inputsListContainer.getChildAt(0).requestFocus()
                    inputsListContainer.animate()
                            .alpha(1.toFloat())
                            .translationY((-50).toFloat())
                            .setInterpolator(DecelerateInterpolator())
                            .setDuration(400)
                            .start()

                    val animFadeOut = AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_out)
                    animFadeOut.duration = 600
                    animFadeOut.interpolator = DecelerateInterpolator()
                    animFadeOut.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) {}
                        override fun onAnimationRepeat(animation: Animation) {}
                        override fun onAnimationEnd(animation: Animation) { backView.visibility = View.INVISIBLE }
                    })

                    backView.startAnimation(animFadeOut)
                }
            }

//            secondaryMenu.findViewById<NumberPicker>(R.id.text_picker).requestFocus()
        } else {
            mainMenu.getChildAt(lastFocusChildIndex).requestFocus()

            inputsListContainer.animate()
                    .alpha(0.toFloat())
                    .translationY(300.toFloat())
                    .setInterpolator(DecelerateInterpolator())
                    .setDuration(400)
                    .start()

            secondaryMenu.animate()
                .alpha(0.toFloat())
                .translationY(300.toFloat())
                .setInterpolator(DecelerateInterpolator())
                .setDuration(400)
                .start()
        }

//        inputsListContainer.animate()
//                .alpha((if (isTop) 1 else 0).toFloat())
//                .translationY((if (isTop) -50 else 300).toFloat())
//                .setInterpolator(DecelerateInterpolator())
//                .setDuration(400)
//                .start()

//        secondaryMenu.animate()
//                .alpha((if (isTop) 1 else 0).toFloat())
//                .translationY((if (isTop) -50 else 300).toFloat())
//                .setInterpolator(DecelerateInterpolator())
//                .setDuration(400)
//                .start()

        mainMenu.animate()
                .translationY((if (!isTop) 0 else 220).toFloat())
                .setInterpolator(DecelerateInterpolator())
                .setDuration(400)
                .start()
    }


    private fun presentPictureModeSettings() {
        val values = arrayOf(
                Pair(R.string.brightness, pictureSettings.brightness),
                Pair(R.string.contrast, pictureSettings.contrast),
                Pair(R.string.saturation, pictureSettings.saturation),
                Pair(R.string.temperature_des, pictureSettings.temperature),
                Pair(R.string.backlight, pictureSettings.backlight)
        )

        var indexOfSelectedPickerItem = 0
        pb.progress = values[indexOfSelectedPickerItem].second
        pbValue.text = values[indexOfSelectedPickerItem].second.toString()

        textPicker.maxValue = 0
        textPicker.displayedValues = values.map { getString(it.first) }.toTypedArray()
        textPicker.maxValue = values.size - 1
        textPicker.minValue = 0

        textPicker.setOnValueChangedListener { _, _, newValue ->
            if (newValue == 3) {
                pb.max = 5
                pb.progressDrawable = ContextCompat.getDrawable(this, R.drawable.gradient_progress_temperature)
            } else {
                pb.max = 100
                pb.progressDrawable = ContextCompat.getDrawable(this, R.drawable.gradient_progress)
            }

            pb.progress = values[newValue].second
            pbValue.text = values[newValue].second.toString()
            indexOfSelectedPickerItem = newValue
        }

        textPicker.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                pb.progress = pb.progress + 1
                pbValue.text = pb.progress.toString()
                values[indexOfSelectedPickerItem] = values[indexOfSelectedPickerItem].copy(second = pb.progress)

                when(indexOfSelectedPickerItem) {
                    0 -> pictureSettings.brightness = pb.progress
                    1 -> pictureSettings.contrast = pb.progress
                    2 -> pictureSettings.saturation = pb.progress
                    3 -> pictureSettings.temperature = pb.progress
                    4 -> pictureSettings.setBacklight(pb.progress, this)
                }

                return@setOnKeyListener true
            }
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (indexOfSelectedPickerItem != 3 || pb.progress != 1) {
                    pb.progress = pb.progress - 1
                }
                pbValue.text = pb.progress.toString()
                values[indexOfSelectedPickerItem] = values[indexOfSelectedPickerItem].copy(second = pb.progress)

                when(indexOfSelectedPickerItem) {
                    0 -> pictureSettings.brightness = pb.progress
                    1 -> pictureSettings.contrast = pb.progress
                    2 -> pictureSettings.saturation = pb.progress
                    3 -> pictureSettings.temperature = pb.progress
                    4 -> pictureSettings.setBacklight(pb.progress, this)
                }

                return@setOnKeyListener true
            }
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                setModeUp(false)
                return@setOnKeyListener true
            }

            false
        }
    }

    private fun presentSoundModeSettings() {
        val values = arrayOf(
                Pair(R.string.sound_height, pictureSettings.getTrebleLevel(this)),
                Pair(R.string.sound_low, pictureSettings.getBassLevel(this)),
                Pair(R.string.sound_balance, pictureSettings.balanceLevel),
                Pair(R.string.sound_height, pictureSettings.getTrebleLevel(this)),
                Pair(R.string.sound_low, pictureSettings.getBassLevel(this)),
                Pair(R.string.sound_balance, pictureSettings.balanceLevel)
        )

        var indexOfSelectedPickerItem = 0
        pb.progress = values[indexOfSelectedPickerItem].second
        pbValue.text = values[indexOfSelectedPickerItem].second.toString()

        textPicker.maxValue = 0
        textPicker.displayedValues = values.map { getString(it.first) }.toTypedArray()
        textPicker.maxValue = values.size - 1
        textPicker.minValue = 0

        textPicker.setOnValueChangedListener { _, _, newValue ->
            pb.max = 100
            pb.progressDrawable = ContextCompat.getDrawable(this, R.drawable.gradient_progress)
            pb.progress = values[newValue].second
            pbValue.text = values[newValue].second.toString()
            indexOfSelectedPickerItem = newValue
        }

        textPicker.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                pb.progress = pb.progress + 1
                pbValue.text = pb.progress.toString()
                values[indexOfSelectedPickerItem] = values[indexOfSelectedPickerItem].copy(second = pb.progress)

                when(indexOfSelectedPickerItem) {
                    0, 3 -> pictureSettings.setTrebleLevel(this, pb.progress)
                    1, 4 -> pictureSettings.setBassLevel(this, pb.progress)
                    2, 5 -> pictureSettings.balanceLevel = pb.progress
                }

                return@setOnKeyListener true
            }
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                pb.progress = pb.progress - 1
                pbValue.text = pb.progress.toString()
                values[indexOfSelectedPickerItem] = values[indexOfSelectedPickerItem].copy(second = pb.progress)

                when(indexOfSelectedPickerItem) {
                    0, 3 -> pictureSettings.setTrebleLevel(this, pb.progress)
                    1, 4 -> pictureSettings.setBassLevel(this, pb.progress)
                    2, 5 -> pictureSettings.balanceLevel = pb.progress
                }

                return@setOnKeyListener true
            }
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                setModeUp(false)
                return@setOnKeyListener true
            }

            false
        }
    }


    var isStartedClosing = false

//    override fun onKey(p0: View?, p1: Int, event: KeyEvent): Boolean {
//        return when {
//            event.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_DPAD_UP -> {
//                setModeUp(true)
//                true
//            }
//
//            event.action == KeyEvent.ACTION_UP && event.keyCode == KeyEvent.KEYCODE_BACK -> {
//                if (isStartedClosing) {
//                    return true
//                }
//
//                isStartedClosing = true
//                mainMenu.animate()/*.alpha(isTop ? 0.9f : 1)*/
//                        .translationY(500f)
//                        .setDuration(400)
//                        .setInterpolator(DecelerateInterpolator())
//                        .start()
//
//                val animFadeOut = AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_out)
//                animFadeOut.duration = 600
//                animFadeOut.interpolator = DecelerateInterpolator()
//                animFadeOut.setAnimationListener(object : Animation.AnimationListener {
//                    override fun onAnimationStart(animation: Animation) {}
//
//                    override fun onAnimationRepeat(animation: Animation) {}
//
//                    override fun onAnimationEnd(animation: Animation) {
//                        stopSelf()
//                    }
//                })
//
//                backView.startAnimation(animFadeOut)
//                true
//            }
//
//            else -> event.keyCode == KeyEvent.KEYCODE_DPAD_UP || event.keyCode == KeyEvent.KEYCODE_BACK
//        }
//    }

    override fun onDestroy() {
        timer.removeCallbacks(updateSleepTime)
        windowManager.removeView(generalView)
        super.onDestroy()
    }

    companion object {
        @Volatile
        var lastUpdate: Long = 0
        private var mainColor = Color.BLUE
        private val OSD_TIME = "OSD_TIME"
    }
}
