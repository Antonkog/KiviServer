package com.wezom.kiviremoteserver.service

import android.annotation.SuppressLint
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
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import com.wezom.kiviremoteserver.App
import com.wezom.kiviremoteserver.R
import com.wezom.kiviremoteserver.common.extensions.animateAnimation
import com.wezom.kiviremoteserver.common.extensions.animateTranslationX
import com.wezom.kiviremoteserver.common.extensions.animateTranslationY
import com.wezom.kiviremoteserver.environment.EnvironmentInputsHelper
import com.wezom.kiviremoteserver.environment.EnvironmentPictureSettings
import com.wezom.kiviremoteserver.service.aspect.data.*
import com.wezom.kiviremoteserver.service.aspect.data.AspectMenuItem.Companion.TYPE_INPUTS
import com.wezom.kiviremoteserver.service.aspect.data.AspectMenuItem.Companion.TYPE_KEYBOARD
import com.wezom.kiviremoteserver.service.aspect.data.AspectMenuItem.Companion.TYPE_PICTURE
import com.wezom.kiviremoteserver.service.aspect.data.AspectMenuItem.Companion.TYPE_SOUND
import com.wezom.kiviremoteserver.service.aspect.recycler.AspectInputsAdapter
import com.wezom.kiviremoteserver.service.aspect.recycler.AspectMainMenuAdapter
import wezom.kiviremoteserver.environment.bridge.BridgePicture
import kotlin.math.abs
import kotlin.math.roundToInt

class AspectLayoutService : Service() {

    private val pictureSettings: EnvironmentPictureSettings = EnvironmentPictureSettings()
    private val inputsHelper: EnvironmentInputsHelper = EnvironmentInputsHelper()
    private val generalType = BridgePicture.LAYER_TYPE//WindowManager.LayoutParams.TYPE_TOAST;

    private var isStartedClosing = false
    private var autoCloseTime = 10
    private val timer = Handler()
    private var lastFocusedMainCardsIndex = 0

    private val windowManager: WindowManager by lazy { baseContext.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private val generalView: RelativeLayout by lazy { View.inflate(baseContext, R.layout.layout_aspect_v2, null) as RelativeLayout }
    private val mainMenu: RecyclerView by lazy { generalView.findViewById<RecyclerView>(R.id.main_menu) }
    private val secondaryMenu: LinearLayout by lazy { generalView.findViewById<LinearLayout>(R.id.secondary_menu) }
    private val backView: View by lazy { generalView.findViewById<View>(R.id.back_view) }

    //Secondary menu with settings
    private val npSubmenuSettingsItems: NumberPicker by lazy { generalView.findViewById<NumberPicker>(R.id.text_picker) }
    private val tvSubmenuSettingValue: TextView by lazy { generalView.findViewById<TextView>(R.id.tv_value) }
    private val pbSubmenuSettingProgress: ProgressBar by lazy { generalView.findViewById<ProgressBar>(R.id.pb) }
    private val ivSubmenuSettingSoundBalanceBar: ImageView by lazy { generalView.findViewById<ImageView>(R.id.iv_progress_sound_balance) }
    private val viewSubmenuSettingSoundBalanceSelector: View by lazy { generalView.findViewById<View>(R.id.view_sound_balance_selector) }
    private val tvSoundBalanceTitleLeft: TextView by lazy { generalView.findViewById<TextView>(R.id.tv_sound_balance_title_left) }
    private val tvSoundBalanceTitleRight: TextView by lazy { generalView.findViewById<TextView>(R.id.tv_sound_balance_title_right) }
    private val soundBarSize: Float by lazy { resources.getDimension(R.dimen.progress_settings_width) }
    private val ivSubmenuSettingPictureTemperatureBar: ImageView by lazy { generalView.findViewById<ImageView>(R.id.iv_progress_temperature) }
    private val viewSubmenuSettingPictureTemperatureSelector: View by lazy { generalView.findViewById<View>(R.id.view_temperature_selector) }
    private val pictureTemperatureBarSize: Float by lazy { resources.getDimension(R.dimen.progress_settings_width) }
    private val pictureTemperatureBarBlockSize: Float by lazy { pictureTemperatureBarSize / 5 }

    //Secondary menu with inputs
    private val rvSubmenuInputs: RecyclerView by lazy { generalView.findViewById<RecyclerView>(R.id.rv_inputs) }
    private val cvSubmenuInputsContainer: CardView by lazy { generalView.findViewById<CardView>(R.id.cv_inputs) }

    //Keyboard
    private val keyboardContainer: LinearLayout by lazy { generalView.findViewById<LinearLayout>(R.id.ll_keyboard) }
    private val keyboardTvChannel: TextView by lazy { generalView.findViewById<TextView>(R.id.tv_channel) }
    private val keyboardBtnErase: FrameLayout by lazy { generalView.findViewById<FrameLayout>(R.id.fl_keyboard_erase) }
    private val keyboardBtnConfirm: FrameLayout by lazy { generalView.findViewById<FrameLayout>(R.id.fl_keyboard_confirm) }
    private val keyboardNumericKeys: List<TextView> by lazy {
        listOf<TextView>(
                generalView.findViewById(R.id.tv_numeric_sym_0),
                generalView.findViewById(R.id.tv_numeric_sym_1),
                generalView.findViewById(R.id.tv_numeric_sym_2),
                generalView.findViewById(R.id.tv_numeric_sym_3),
                generalView.findViewById(R.id.tv_numeric_sym_4),
                generalView.findViewById(R.id.tv_numeric_sym_5),
                generalView.findViewById(R.id.tv_numeric_sym_6),
                generalView.findViewById(R.id.tv_numeric_sym_7),
                generalView.findViewById(R.id.tv_numeric_sym_8),
                generalView.findViewById(R.id.tv_numeric_sym_9)
        )
    }


    private val updateSleepTimeCallback = object : Runnable {
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
        mainColor = ResourcesCompat.getColor(resources, R.color.colorPrimary, null)

        lastUpdate = System.currentTimeMillis()
        initLayout()

        timer.postDelayed(updateSleepTimeCallback, autoCloseTime.toLong())
    }

    override fun onBind(intent: Intent): IBinder? = null

    @Suppress("UsePropertyAccessSyntax")
    @SuppressLint("SetTextI18n")
    private fun initLayout() {
        val param = WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, generalType, WindowManager.LayoutParams.FLAG_FULLSCREEN, PixelFormat.TRANSLUCENT)
        windowManager.addView(generalView, param)
        generalView.visibility = View.VISIBLE
        generalView.clearAnimation()

        viewSubmenuSettingPictureTemperatureSelector.translationX = when(pictureSettings.temperature) {
            PICTURE_TEMPERATURE_MODE_NORMAL -> 0f
            PICTURE_TEMPERATURE_MODE_WARM -> pictureTemperatureBarBlockSize
            PICTURE_TEMPERATURE_MODE_VERY_WARM -> pictureTemperatureBarBlockSize * 2
            PICTURE_TEMPERATURE_MODE_COLD -> pictureTemperatureBarBlockSize * -1
            PICTURE_TEMPERATURE_MODE_VERY_COLD -> pictureTemperatureBarBlockSize * -2
            else -> 0f
        }

        val balanceLevel = pictureSettings.getBalanceLevel(baseContext)
        viewSubmenuSettingSoundBalanceSelector.translationX = when (balanceLevel) {
            50 -> 0f
            else -> soundBarSize / 2 * ((balanceLevel - 50).toFloat() / 50f)
        }

        val mainMenuCardsAdapter = AspectMainMenuAdapter(AspectMenuItems.allData.values.toList(), pictureSettings,
                onDpadUpClick = { data -> setModeUp(true, data) },
                onBackClick = { onMainCardsBackClick() },
                onSettingsClick = { onSettingsClick() },
                onKeyboardClick = { onKeyboardClick() })
        mainMenu.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mainMenu.adapter = mainMenuCardsAdapter

        val inputsAdapter = AspectInputsAdapter(cvSubmenuInputsContainer.context, inputsHelper, onBackClick = {
            setModeUp(false)
            backView.animateAnimation(applicationContext, android.R.anim.fade_in, 600, onAnimationEnd = { backView.setVisibility(View.VISIBLE) })
        })
        rvSubmenuInputs.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvSubmenuInputs.adapter = inputsAdapter

        keyboardNumericKeys.forEach {
            it.setOnClickListener {
                timeoutHandler.removeCallbacks(channelTypingTimeout)
                timeoutHandler.postDelayed(channelTypingTimeout, CHANNEL_TYPING_TIMEOUT)

                if (keyboardTvChannel.text.length < 3) {
                    keyboardTvChannel.text = "${keyboardTvChannel.text}${(it as TextView).text}"
                } else {
                    keyboardTvChannel.text = (it as TextView).text.toString()
                }
            }

            it.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    timeoutHandler.removeCallbacks(channelTypingTimeout)
                    keyboardContainer.animateTranslationY(640f, 400)
                    keyboardContainer.animateAnimation(applicationContext, android.R.anim.fade_out, 400, onAnimationEnd = {
                        keyboardContainer.visibility = View.GONE
                        backView.visibility = View.GONE
                        stopSelf()
                    })

                    return@setOnKeyListener true
                }

                else if (event.action == KeyEvent.ACTION_DOWN && keyCode in arrayOf(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN)) {
                    timeoutHandler.removeCallbacks(channelTypingTimeout)
                    timeoutHandler.postDelayed(channelTypingTimeout, CHANNEL_TYPING_TIMEOUT)
                    return@setOnKeyListener false
                }

                return@setOnKeyListener false
            }
        }

        keyboardBtnErase.setOnClickListener {
            if (keyboardTvChannel.text.isNotEmpty()) {
                timeoutHandler.removeCallbacks(channelTypingTimeout)
                timeoutHandler.postDelayed(channelTypingTimeout, CHANNEL_TYPING_TIMEOUT)
                keyboardTvChannel.text = keyboardTvChannel.text.subSequence(0, keyboardTvChannel.text.length - 1)
            }
        }

        keyboardBtnConfirm.setOnClickListener {
            timeoutHandler.removeCallbacks(channelTypingTimeout)
            changeTvProgram()
        }

        mainMenu.animateTranslationY(0f, 400)
        backView.animateAnimation(applicationContext, android.R.anim.fade_in, 600)
    }

    private val timeoutHandler = Handler()
    private val channelTypingTimeout = Runnable { changeTvProgram() }
    private fun changeTvProgram() {
        if (keyboardTvChannel.text.isEmpty()) { return }
        EnvironmentInputsHelper().changeProgram(keyboardTvChannel.text.toString().toInt(), this)
        stopSelf()
    }

    private fun onMainCardsBackClick() {
        if (isStartedClosing) { return }
        isStartedClosing = true
        mainMenu.animateTranslationY(500f, 400)
        backView.animateAnimation(applicationContext, android.R.anim.fade_out, 600, onAnimationEnd = {
            backView.visibility = View.GONE
            stopSelf()
        })
    }

    private fun onSettingsClick() {
        if (isStartedClosing) { return }
        isStartedClosing = true
        mainMenu.animateTranslationY(500f, 400)
        backView.animateAnimation(applicationContext, android.R.anim.fade_out, 600,
                onAnimationStart = { startActivity(Intent(Settings.ACTION_SETTINGS)) },
                onAnimationEnd = {
                    backView.visibility = View.GONE
                    stopSelf()
                }
        )
    }

    private fun onKeyboardClick() {
        setModeUp(true, AspectMenuItems.allData[TYPE_KEYBOARD])
    }

    @Suppress("UsePropertyAccessSyntax")
    private fun setModeUp(isTop: Boolean, data: AspectMenuItem? = null) {
        if (isTop) {
            lastFocusedMainCardsIndex = mainMenu.indexOfChild(mainMenu.focusedChild)

            when (data?.type) {
                TYPE_PICTURE -> {
                    presentPictureModeSettings()
                    npSubmenuSettingsItems.requestFocus()
                    secondaryMenu.animateTranslationY(-50f, 400, alpha = 1f)
                }
                TYPE_SOUND -> {
                    presentSoundModeSettings()
                    npSubmenuSettingsItems.requestFocus()
                    secondaryMenu.animateTranslationY(-50f, 400, alpha = 1f)
                }
                TYPE_INPUTS -> {
                    cvSubmenuInputsContainer.getChildAt(0).requestFocus()
                    cvSubmenuInputsContainer.animateTranslationY(-50f, 400, alpha = 1f)
                    backView.animateAnimation(applicationContext, android.R.anim.fade_out, 600, onAnimationEnd = { backView.setVisibility(View.INVISIBLE) })
                }
                TYPE_KEYBOARD -> {
                    backView.animateAnimation(applicationContext, android.R.anim.fade_out, 400, onAnimationEnd = { backView.setVisibility(View.INVISIBLE) })
                    keyboardNumericKeys[5].requestFocus()
                    keyboardContainer.animateTranslationY(0f, 400)
                    mainMenu.animateTranslationY(500f, 400)
                    return
                }
            }
        } else {
            mainMenu.getChildAt(lastFocusedMainCardsIndex).requestFocus()
            cvSubmenuInputsContainer.animateTranslationY(300f, 400, alpha = 0f)
            secondaryMenu.animateTranslationY(300f, 400, alpha = 0f)
        }

        mainMenu.animateTranslationY(if (!isTop) 0f else 220f, 400)
    }

    private fun presentPictureModeSettings() {
        val settingsValues = arrayOf(
                Pair(R.string.brightness, pictureSettings.brightness),
                Pair(R.string.contrast, pictureSettings.contrast),
                Pair(R.string.saturation, pictureSettings.saturation),
                Pair(R.string.temperature_des, pictureSettings.temperature),
                Pair(R.string.backlight, pictureSettings.backlight)
        )

        var indexOfSelectedPickerItem = 0
        pbSubmenuSettingProgress.progress = settingsValues[indexOfSelectedPickerItem].second
        tvSubmenuSettingValue.text = settingsValues[indexOfSelectedPickerItem].second.toString()
        npSubmenuSettingsItems.apply {
            maxValue = 0
            displayedValues = settingsValues.map { getString(it.first) }.toTypedArray()
            maxValue = settingsValues.size - 1
            minValue = 0
        }

        npSubmenuSettingsItems.setOnValueChangedListener { _, _, newValue ->
            changeSubmenuSettingsPictureItem(newValue, settingsValues)
            indexOfSelectedPickerItem = newValue
        }

        npSubmenuSettingsItems.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                changeSubmenuSettingsPictureItemValue(indexOfSelectedPickerItem, settingsValues, true)
                return@setOnKeyListener true
            }

            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                changeSubmenuSettingsPictureItemValue(indexOfSelectedPickerItem, settingsValues, false)
                return@setOnKeyListener true
            }

            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                submenuSettingsPictureBackClick()
                return@setOnKeyListener true
            }

            return@setOnKeyListener false
        }
    }

    private fun presentSoundModeSettings() {
        val settingsValues = arrayOf(
                Pair(R.string.sound_height, pictureSettings.getTrebleLevel(this)),
                Pair(R.string.sound_low, pictureSettings.getBassLevel(this)),
                Pair(R.string.sound_balance, pictureSettings.getBalanceLevel(this)),
                Pair(R.string.sound_height, pictureSettings.getTrebleLevel(this)),
                Pair(R.string.sound_low, pictureSettings.getBassLevel(this)),
                Pair(R.string.sound_balance, pictureSettings.getBalanceLevel(this))
        )

        var indexOfSelectedPickerItem = 0
        pbSubmenuSettingProgress.progress = settingsValues[indexOfSelectedPickerItem].second
        tvSubmenuSettingValue.text = settingsValues[indexOfSelectedPickerItem].second.toString()
        npSubmenuSettingsItems.apply {
            maxValue = 0
            displayedValues = settingsValues.map { getString(it.first) }.toTypedArray()
            maxValue = settingsValues.size - 1
            minValue = 0
        }

        npSubmenuSettingsItems.setOnValueChangedListener { _, _, newValue ->
            changeSubmenuSettingsSoundItem(settingsValues, newValue)
            indexOfSelectedPickerItem = newValue
        }

        npSubmenuSettingsItems.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                changeSubmenuSettingsSoundItemValue(indexOfSelectedPickerItem, settingsValues, true)
                return@setOnKeyListener true
            }

            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                changeSubmenuSettingsSoundItemValue(indexOfSelectedPickerItem, settingsValues, false)
                return@setOnKeyListener true
            }

            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                submenuSettingsSoundBackClick()
                return@setOnKeyListener true
            }

            return@setOnKeyListener false
        }
    }

    private fun changeSubmenuSettingsPictureItem(newItemValue: Int, settingsValues: Array<Pair<Int, Int>>) {
        if (newItemValue == PICKER_INDEX_PICTURE_TEMPERATURE) {
            pbSubmenuSettingProgress.visibility = View.GONE
            ivSubmenuSettingPictureTemperatureBar.visibility = View.VISIBLE
            viewSubmenuSettingPictureTemperatureSelector.visibility = View.VISIBLE

            val newTempBarText = when(pictureSettings.temperature) {
                PICTURE_TEMPERATURE_MODE_NORMAL -> R.string.temperature_mode_normal
                PICTURE_TEMPERATURE_MODE_WARM -> R.string.temperature_mode_warm
                PICTURE_TEMPERATURE_MODE_VERY_WARM -> R.string.temperature_mode_very_warm
                PICTURE_TEMPERATURE_MODE_COLD -> R.string.temperature_mode_cold
                PICTURE_TEMPERATURE_MODE_VERY_COLD -> R.string.temperature_mode_very_cold
                else -> R.string.temperature_mode_normal
            }

            tvSubmenuSettingValue.setText(newTempBarText)
            return
        }

        ivSubmenuSettingPictureTemperatureBar.visibility = View.GONE
        viewSubmenuSettingPictureTemperatureSelector.visibility = View.GONE
        tvSubmenuSettingValue.text = settingsValues[newItemValue].second.toString()
        pbSubmenuSettingProgress.apply {
            visibility = View.VISIBLE
            max = 100
            progressDrawable = ContextCompat.getDrawable(this.context, R.drawable.gradient_progress)
            progress = settingsValues[newItemValue].second
        }
    }

    private fun changeSubmenuSettingsPictureItemValue(indexOfSelectedPickerItem: Int, settingsValues: Array<Pair<Int, Int>>, isIncrementOperation: Boolean) {
        if (indexOfSelectedPickerItem == PICKER_INDEX_PICTURE_TEMPERATURE) {
            pictureSettings.temperature = when(pictureSettings.temperature) {
                PICTURE_TEMPERATURE_MODE_NORMAL -> if (isIncrementOperation) PICTURE_TEMPERATURE_MODE_WARM else PICTURE_TEMPERATURE_MODE_COLD
                PICTURE_TEMPERATURE_MODE_WARM -> if (isIncrementOperation) PICTURE_TEMPERATURE_MODE_VERY_WARM else PICTURE_TEMPERATURE_MODE_NORMAL
                PICTURE_TEMPERATURE_MODE_VERY_WARM -> if (isIncrementOperation) return else PICTURE_TEMPERATURE_MODE_WARM
                PICTURE_TEMPERATURE_MODE_COLD -> if (isIncrementOperation) PICTURE_TEMPERATURE_MODE_NORMAL else PICTURE_TEMPERATURE_MODE_VERY_COLD
                PICTURE_TEMPERATURE_MODE_VERY_COLD -> if (isIncrementOperation) PICTURE_TEMPERATURE_MODE_COLD else return
                else -> PICTURE_TEMPERATURE_MODE_NORMAL
            }

            val newTempBarTransX = when(pictureSettings.temperature) {
                PICTURE_TEMPERATURE_MODE_NORMAL -> 0f
                PICTURE_TEMPERATURE_MODE_WARM -> pictureTemperatureBarBlockSize
                PICTURE_TEMPERATURE_MODE_VERY_WARM -> pictureTemperatureBarBlockSize * 2
                PICTURE_TEMPERATURE_MODE_COLD -> pictureTemperatureBarBlockSize * -1
                PICTURE_TEMPERATURE_MODE_VERY_COLD -> pictureTemperatureBarBlockSize * -2
                else -> 0f
            }

            val newTempBarText = when(pictureSettings.temperature) {
                PICTURE_TEMPERATURE_MODE_NORMAL -> R.string.temperature_mode_normal
                PICTURE_TEMPERATURE_MODE_WARM -> R.string.temperature_mode_warm
                PICTURE_TEMPERATURE_MODE_VERY_WARM -> R.string.temperature_mode_very_warm
                PICTURE_TEMPERATURE_MODE_COLD -> R.string.temperature_mode_cold
                PICTURE_TEMPERATURE_MODE_VERY_COLD -> R.string.temperature_mode_very_cold
                else -> R.string.temperature_mode_normal
            }

            viewSubmenuSettingPictureTemperatureSelector.animateTranslationX(newTempBarTransX, 150)
            tvSubmenuSettingValue.setText(newTempBarText)
            return
        }

        if (isIncrementOperation) {
            pbSubmenuSettingProgress.progress++
        } else {
            pbSubmenuSettingProgress.progress--
        }

        tvSubmenuSettingValue.text = pbSubmenuSettingProgress.progress.toString()
        settingsValues[indexOfSelectedPickerItem] = settingsValues[indexOfSelectedPickerItem].copy(second = pbSubmenuSettingProgress.progress)

        when(indexOfSelectedPickerItem) {
            PICKER_INDEX_PICTURE_BRIGHTNESS -> pictureSettings.brightness = pbSubmenuSettingProgress.progress
            PICKER_INDEX_PICTURE_CONTRAST -> pictureSettings.contrast = pbSubmenuSettingProgress.progress
            PICKER_INDEX_PICTURE_SATURATION -> pictureSettings.saturation = pbSubmenuSettingProgress.progress
            PICKER_INDEX_PICTURE_BACKLIGHT -> pictureSettings.setBacklight(pbSubmenuSettingProgress.progress, this)
        }

        return
    }

    private fun submenuSettingsPictureBackClick() {
        setModeUp(false)
        pbSubmenuSettingProgress.visibility = View.VISIBLE
        ivSubmenuSettingPictureTemperatureBar.visibility = View.GONE
        viewSubmenuSettingPictureTemperatureSelector.visibility = View.GONE
    }

    private fun changeSubmenuSettingsSoundItem(settingsValues: Array<Pair<Int, Int>>, newValue: Int) {
        if (newValue == PICKER_INDEX_SOUND_BALANCE_1 || newValue == PICKER_INDEX_SOUND_BALANCE_2) {
            pbSubmenuSettingProgress.visibility = View.GONE
            ivSubmenuSettingSoundBalanceBar.visibility = View.VISIBLE
            viewSubmenuSettingSoundBalanceSelector.visibility = View.VISIBLE
            tvSoundBalanceTitleLeft.visibility = View.VISIBLE
            tvSoundBalanceTitleRight.visibility = View.VISIBLE

            val balanceLevel = pictureSettings.getBalanceLevel(baseContext)
            val balancePercentValue = abs(balanceLevel - 50) / 50f * 100
            tvSubmenuSettingValue.text = when {
                balanceLevel > 50 -> getString(R.string.sound_balance_right_value, balancePercentValue.roundToInt())
                balanceLevel < 50 -> getString(R.string.sound_balance_left_value, balancePercentValue.roundToInt())
                else -> ""
            }

            return
        }

        ivSubmenuSettingSoundBalanceBar.visibility = View.GONE
        viewSubmenuSettingSoundBalanceSelector.visibility = View.GONE
        tvSoundBalanceTitleLeft.visibility = View.GONE
        tvSoundBalanceTitleRight.visibility = View.GONE

        tvSubmenuSettingValue.text = settingsValues[newValue].second.toString()
        pbSubmenuSettingProgress.apply {
            visibility = View.VISIBLE
            max = 100
            progressDrawable = ContextCompat.getDrawable(this.context, R.drawable.gradient_progress)
            progress = settingsValues[newValue].second
        }
    }

    private fun changeSubmenuSettingsSoundItemValue(indexOfSelectedPickerItem: Int, settingsValues: Array<Pair<Int, Int>>, isIncrementOperation: Boolean) {
        if (indexOfSelectedPickerItem == PICKER_INDEX_SOUND_BALANCE_1 || indexOfSelectedPickerItem == PICKER_INDEX_SOUND_BALANCE_2) {
            var balanceLevel = pictureSettings.getBalanceLevel(baseContext)
            if (isIncrementOperation) {
                if (balanceLevel == 100) { return }
                balanceLevel++
                pictureSettings.setBalanceLevel(baseContext, balanceLevel)
            } else {
                if (balanceLevel == 0) { return }
                balanceLevel--
                pictureSettings.setBalanceLevel(baseContext, balanceLevel)
            }

            val balancePercentValue = abs(balanceLevel - 50) / 50f * 100
            tvSubmenuSettingValue.text = when {
                balanceLevel > 50 -> getString(R.string.sound_balance_right_value, balancePercentValue.roundToInt())
                balanceLevel < 50 -> getString(R.string.sound_balance_left_value, balancePercentValue.roundToInt())
                else -> ""
            }

            viewSubmenuSettingSoundBalanceSelector.translationX = when (balanceLevel) {
                50 -> 0f
                else -> soundBarSize / 2 * ((balanceLevel - 50).toFloat() / 50f)
            }

            return
        }

        if (isIncrementOperation) {
            pbSubmenuSettingProgress.progress++
        } else {
            pbSubmenuSettingProgress.progress--
        }

        tvSubmenuSettingValue.text = pbSubmenuSettingProgress.progress.toString()

        when(indexOfSelectedPickerItem) {
            PICKER_INDEX_SOUND_TREBLE_1, PICKER_INDEX_SOUND_TREBLE_2 -> {
                pictureSettings.setTrebleLevel(this, pbSubmenuSettingProgress.progress)
                settingsValues[PICKER_INDEX_SOUND_TREBLE_1] = settingsValues[PICKER_INDEX_SOUND_TREBLE_1].copy(second = pbSubmenuSettingProgress.progress)
                settingsValues[PICKER_INDEX_SOUND_TREBLE_2] = settingsValues[PICKER_INDEX_SOUND_TREBLE_2].copy(second = pbSubmenuSettingProgress.progress)
            }
            PICKER_INDEX_SOUND_BASS_1, PICKER_INDEX_SOUND_BASS_2 -> {
                pictureSettings.setBassLevel(this, pbSubmenuSettingProgress.progress)
                settingsValues[PICKER_INDEX_SOUND_BASS_1] = settingsValues[PICKER_INDEX_SOUND_BASS_1].copy(second = pbSubmenuSettingProgress.progress)
                settingsValues[PICKER_INDEX_SOUND_BASS_2] = settingsValues[PICKER_INDEX_SOUND_BASS_2].copy(second = pbSubmenuSettingProgress.progress)
            }
        }
    }

    private fun submenuSettingsSoundBackClick() {
        setModeUp(false)
        pbSubmenuSettingProgress.visibility = View.VISIBLE
        ivSubmenuSettingSoundBalanceBar.visibility = View.GONE
        viewSubmenuSettingSoundBalanceSelector.visibility = View.GONE
        tvSoundBalanceTitleLeft.visibility = View.GONE
        tvSoundBalanceTitleRight.visibility = View.GONE
    }

    override fun onDestroy() {
        timer.removeCallbacks(updateSleepTimeCallback)
        windowManager.removeView(generalView)
        super.onDestroy()
    }

    companion object {
        @Volatile
        var lastUpdate: Long = 0
        private var mainColor = Color.BLUE
        private const val OSD_TIME = "OSD_TIME"
        private const val CHANNEL_TYPING_TIMEOUT = 2000L

        private const val PICKER_INDEX_PICTURE_BRIGHTNESS = 0
        private const val PICKER_INDEX_PICTURE_CONTRAST = 1
        private const val PICKER_INDEX_PICTURE_SATURATION = 2
        private const val PICKER_INDEX_PICTURE_TEMPERATURE = 3
        private const val PICKER_INDEX_PICTURE_BACKLIGHT = 4

        private const val PICKER_INDEX_SOUND_TREBLE_1 = 0
        private const val PICKER_INDEX_SOUND_TREBLE_2 = 3
        private const val PICKER_INDEX_SOUND_BASS_1 = 1
        private const val PICKER_INDEX_SOUND_BASS_2 = 4
        private const val PICKER_INDEX_SOUND_BALANCE_1 = 2
        private const val PICKER_INDEX_SOUND_BALANCE_2 = 5
    }

}