package com.wezom.kiviremoteserver.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper
import wezom.kiviremoteserver.environment.bridge.BridgePicture
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.roundToInt

class AspectLayoutService : Service() {

    //Environment settings
    private val pictureSettings = WeakReference(EnvironmentPictureSettings())
    private val inputsHelper = WeakReference(EnvironmentInputsHelper())

    //Auto closing
    private var isStartedClosing = false
    private var autoCloseTime = 10
    private val sleepTimer = Handler()
    private val updateSleepTimeCallback = object : Runnable {
        override fun run() {
            sleepTimer.postDelayed(this, 1000)
            if (autoCloseTime > 0 && System.currentTimeMillis() - autoCloseTime > lastUpdate)  {
                stopSelf()
            }
        }
    }

    //Main views
    private val windowManager: WeakReference<WindowManager> by lazy { WeakReference(getSystemService(WINDOW_SERVICE) as WindowManager) }
    private val generalView: WeakReference<RelativeLayout> by lazy { WeakReference(View.inflate(baseContext, R.layout.layout_aspect_v2, null) as RelativeLayout) }
    private val mainMenu: WeakReference<RecyclerView> by lazy { WeakReference(generalView.get()!!.findViewById<RecyclerView>(R.id.main_menu)) }
    private var lastFocusedMainCardsIndex = 0
    private val secondaryMenu: WeakReference<LinearLayout> by lazy { WeakReference(generalView.get()!!.findViewById<LinearLayout>(R.id.secondary_menu)) }
    private val backView: WeakReference<View> by lazy { WeakReference(generalView.get()!!.findViewById<View>(R.id.back_view)) }

    //Secondary menu with settings
    private val npSubmenuSettingsItems: WeakReference<NumberPicker> by lazy { WeakReference(generalView.get()!!.findViewById<NumberPicker>(R.id.text_picker)) }
    private val tvSubmenuSettingValue: WeakReference<TextView> by lazy { WeakReference(generalView.get()!!.findViewById<TextView>(R.id.tv_value)) }
    private val pbSubmenuSettingProgress: WeakReference<ProgressBar> by lazy { WeakReference(generalView.get()!!.findViewById<ProgressBar>(R.id.pb)) }
    private val ivSubmenuSettingSoundBalanceBar: WeakReference<ImageView> by lazy { WeakReference(generalView.get()!!.findViewById<ImageView>(R.id.iv_progress_sound_balance)) }
    private val viewSubmenuSettingSoundBalanceSelector: WeakReference<View> by lazy { WeakReference(generalView.get()!!.findViewById<View>(R.id.view_sound_balance_selector)) }
    private val tvSoundBalanceTitleLeft: WeakReference<TextView> by lazy { WeakReference(generalView.get()!!.findViewById<TextView>(R.id.tv_sound_balance_title_left)) }
    private val tvSoundBalanceTitleRight: WeakReference<TextView> by lazy { WeakReference(generalView.get()!!.findViewById<TextView>(R.id.tv_sound_balance_title_right)) }
    private val soundBarSize: Float by lazy { resources.getDimension(R.dimen.progress_settings_width) }
    private val ivSubmenuSettingPictureTemperatureBar: WeakReference<ImageView> by lazy { WeakReference(generalView.get()!!.findViewById<ImageView>(R.id.iv_progress_temperature)) }
    private val viewSubmenuSettingPictureTemperatureSelector: WeakReference<View> by lazy { WeakReference(generalView.get()!!.findViewById<View>(R.id.view_temperature_selector)) }
    private val pictureTemperatureBarSize: Float by lazy { resources.getDimension(R.dimen.progress_settings_width) }
    private val pictureTemperatureBarBlockSize: Float by lazy { pictureTemperatureBarSize / 5 }

    //Secondary menu with inputs
    private val rvSubmenuInputs: WeakReference<RecyclerView> by lazy { WeakReference(generalView.get()!!.findViewById<RecyclerView>(R.id.rv_inputs)) }
    private val cvSubmenuInputsContainer: WeakReference<CardView> by lazy { WeakReference(generalView.get()!!.findViewById<CardView>(R.id.cv_inputs)) }

    //Keypad
    private val keyboardContainer: WeakReference<LinearLayout> by lazy { WeakReference(generalView.get()!!.findViewById<LinearLayout>(R.id.ll_keyboard)) }
    private val keyboardTvChannel: WeakReference<TextView> by lazy { WeakReference(generalView.get()!!.findViewById<TextView>(R.id.tv_channel)) }
    private val keyboardBtnErase: WeakReference<FrameLayout> by lazy { WeakReference(generalView.get()!!.findViewById<FrameLayout>(R.id.fl_keyboard_erase)) }
    private val keyboardBtnConfirm: WeakReference<FrameLayout> by lazy { WeakReference(generalView.get()!!.findViewById<FrameLayout>(R.id.fl_keyboard_confirm)) }
    private val typingTimer = Handler()
    private val channelTypingTimeout = Runnable { changeTvProgram() }

    @Suppress("RemoveExplicitTypeArguments")
    private val keyboardNumericKeys: List<WeakReference<TextView>> by lazy {
        listOf(
                WeakReference(generalView.get()!!.findViewById<TextView>(R.id.tv_numeric_sym_0)),
                WeakReference(generalView.get()!!.findViewById<TextView>(R.id.tv_numeric_sym_1)),
                WeakReference(generalView.get()!!.findViewById<TextView>(R.id.tv_numeric_sym_2)),
                WeakReference(generalView.get()!!.findViewById<TextView>(R.id.tv_numeric_sym_3)),
                WeakReference(generalView.get()!!.findViewById<TextView>(R.id.tv_numeric_sym_4)),
                WeakReference(generalView.get()!!.findViewById<TextView>(R.id.tv_numeric_sym_5)),
                WeakReference(generalView.get()!!.findViewById<TextView>(R.id.tv_numeric_sym_6)),
                WeakReference(generalView.get()!!.findViewById<TextView>(R.id.tv_numeric_sym_7)),
                WeakReference(generalView.get()!!.findViewById<TextView>(R.id.tv_numeric_sym_8)),
                WeakReference(generalView.get()!!.findViewById<TextView>(R.id.tv_numeric_sym_9))
        )
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && App.checkWizard(this)) {
            return
        }

        initLayout()
        initAutoClose()
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun initAutoClose() {
        autoCloseTime = Settings.Global.getInt(contentResolver, OSD_TIME, 10)
        if (autoCloseTime in 1..9) { autoCloseTime = 10 }

        //TODO remove autoCloseTime *= 10;
        autoCloseTime *= 1000000
        autoCloseTime *= 1000

        lastUpdate = System.currentTimeMillis()
        sleepTimer.postDelayed(updateSleepTimeCallback, autoCloseTime.toLong())
    }

    @Suppress("UsePropertyAccessSyntax")
    @SuppressLint("SetTextI18n", "WrongConstant")
    private fun initLayout() {
        generalView.get()!!.visibility = View.VISIBLE
        generalView.get()!!.clearAnimation()

        initSettingsViewIndicator()
        initRecyclerViews()
        initClickListeners()

        val param = WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                BridgePicture.LAYER_TYPE,
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT)
        windowManager.get()!!.addView(generalView.get()!!, param)

        mainMenu.get()!!.animateTranslationY(0f, 400)
        backView.get()!!.animateAnimation(baseContext, android.R.anim.fade_in, 600)
    }

    private fun initSettingsViewIndicator() {
        viewSubmenuSettingPictureTemperatureSelector.get()!!.translationX = when(pictureSettings.get()!!.temperature) {
            PICTURE_TEMPERATURE_MODE_NORMAL -> 0f
            PICTURE_TEMPERATURE_MODE_WARM -> pictureTemperatureBarBlockSize
            PICTURE_TEMPERATURE_MODE_VERY_WARM -> pictureTemperatureBarBlockSize * 2
            PICTURE_TEMPERATURE_MODE_COLD -> pictureTemperatureBarBlockSize * -1
            PICTURE_TEMPERATURE_MODE_VERY_COLD -> pictureTemperatureBarBlockSize * -2
            else -> 0f
        }

        val balanceLevel = pictureSettings.get()!!.getBalanceLevel(baseContext)
        viewSubmenuSettingSoundBalanceSelector.get()!!.translationX = when (balanceLevel) {
            50 -> 0f
            else -> soundBarSize / 2 * ((balanceLevel - 50).toFloat() / 50f)
        }
    }

    private fun initRecyclerViews() {
        val mainMenuCardsAdapter = AspectMainMenuAdapter(AspectMenuItems.allData.values.toList(), pictureSettings.get()!!,
                onDpadUpClick = { data -> setModeUp(true, data) },
                onBackClick = { onMainCardsBackClick() },
                onSettingsClick = { onSettingsClick() },
                onKeyboardClick = { onKeyboardClick() })

        mainMenu.get()!!.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        mainMenu.get()!!.adapter = mainMenuCardsAdapter

        val inputsAdapter = AspectInputsAdapter(inputsHelper.get()!!.getPortsList(arrayListOf<InputSourceHelper.INPUT_PORT>(), baseContext), inputsHelper.get()!!, onBackClick = {
            setModeUp(false)
            backView.get()!!.animateAnimation(baseContext, android.R.anim.fade_in, 600, onAnimationEnd = { backView.get()!!.setVisibility(View.VISIBLE) })
        })
        rvSubmenuInputs.get()!!.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        rvSubmenuInputs.get()!!.adapter = inputsAdapter
    }

    private fun initClickListeners() {
        keyboardNumericKeys.forEach {
            it.get()!!.setOnClickListener {
                typingTimer.removeCallbacks(channelTypingTimeout)
                typingTimer.postDelayed(channelTypingTimeout, CHANNEL_TYPING_TIMEOUT)

                if (keyboardTvChannel.get()!!.text.length < 3) {
                    keyboardTvChannel.get()!!.text = "${keyboardTvChannel.get()!!.text}${(it as TextView).text}"
                } else {
                    keyboardTvChannel.get()!!.text = (it as TextView).text.toString()
                }
            }

            it.get()!!.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    typingTimer.removeCallbacks(channelTypingTimeout)
                    keyboardContainer.get()!!.animateTranslationY(640f, 400)
                    keyboardContainer.get()!!.animateAnimation(baseContext, android.R.anim.fade_out, 400, onAnimationEnd = {
                        keyboardContainer.get()!!.visibility = View.GONE
                        backView.get()!!.visibility = View.GONE
                        stopSelf()
                    })

                    return@setOnKeyListener true
                }

                else if (event.action == KeyEvent.ACTION_DOWN && keyCode in arrayOf(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN)) {
                    typingTimer.removeCallbacks(channelTypingTimeout)
                    typingTimer.postDelayed(channelTypingTimeout, CHANNEL_TYPING_TIMEOUT)
                    return@setOnKeyListener false
                }

                return@setOnKeyListener false
            }
        }

        keyboardBtnErase.get()!!.setOnClickListener {
            if (keyboardTvChannel.get()!!.text.isNotEmpty()) {
                typingTimer.removeCallbacks(channelTypingTimeout)
                typingTimer.postDelayed(channelTypingTimeout, CHANNEL_TYPING_TIMEOUT)
                keyboardTvChannel.get()!!.text = keyboardTvChannel.get()!!.text.subSequence(0, keyboardTvChannel.get()!!.text.length - 1)
            }
        }

        keyboardBtnConfirm.get()!!.setOnClickListener {
            typingTimer.removeCallbacks(channelTypingTimeout)
            changeTvProgram()
        }
    }

    private fun changeTvProgram() {
        if (keyboardTvChannel.get()!!.text.isEmpty()) { return }
        EnvironmentInputsHelper().changeProgram(keyboardTvChannel.get()!!.text.toString().toInt(), this)
        stopSelf()
    }

    private fun onMainCardsBackClick() {
        if (isStartedClosing) { return }
        isStartedClosing = true
        mainMenu.get()!!.animateTranslationY(500f, 400)
        backView.get()!!.animateAnimation(baseContext, android.R.anim.fade_out, 600, onAnimationEnd = {
            backView.get()!!.visibility = View.GONE
            stopSelf()
        })
    }

    private fun onSettingsClick() {
        if (isStartedClosing) { return }
        isStartedClosing = true
        mainMenu.get()!!.animateTranslationY(500f, 400)
        backView.get()!!.animateAnimation(baseContext, android.R.anim.fade_out, 600,
                onAnimationStart = { startActivity(Intent(Settings.ACTION_SETTINGS)) },
                onAnimationEnd = {
                    backView.get()!!.visibility = View.GONE
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
            lastFocusedMainCardsIndex = mainMenu.get()!!.indexOfChild(mainMenu.get()!!.focusedChild)

            when (data?.type) {
                TYPE_PICTURE -> {
                    presentPictureModeSettings()
                    npSubmenuSettingsItems.get()!!.requestFocus()
                    secondaryMenu.get()!!.visibility = View.VISIBLE
                    secondaryMenu.get()!!.animateTranslationY(-50f, 400, alpha = 1f)
                }
                TYPE_SOUND -> {
                    presentSoundModeSettings()
                    npSubmenuSettingsItems.get()!!.requestFocus()
                    secondaryMenu.get()!!.visibility = View.VISIBLE
                    secondaryMenu.get()!!.animateTranslationY(-50f, 400, alpha = 1f)
                }
                TYPE_INPUTS -> {
                    cvSubmenuInputsContainer.get()!!.visibility = View.VISIBLE
                    cvSubmenuInputsContainer.get()!!.getChildAt(0).requestFocus()
                    cvSubmenuInputsContainer.get()!!.animateTranslationY(-50f, 400, alpha = 1f)
                    backView.get()!!.animateAnimation(baseContext, android.R.anim.fade_out, 600, onAnimationEnd = { backView.get()!!.setVisibility(View.INVISIBLE) })
                }
                TYPE_KEYBOARD -> {
                    mainMenu.get()!!.visibility = View.GONE
                    backView.get()!!.animateAnimation(baseContext, android.R.anim.fade_out, 400, onAnimationEnd = { backView.get()!!.setVisibility(View.INVISIBLE) })
                    keyboardNumericKeys[5].get()!!.requestFocus()
                    keyboardContainer.get()!!.animateTranslationY(0f, 400)
                    mainMenu.get()!!.animateTranslationY(500f, 400)
                    return
                }
            }

            mainMenu.get()!!.animateTranslationY(220f, 400)
        } else {
            cvSubmenuInputsContainer.get()!!.animateTranslationY(300f, 400, alpha = 0f)
            secondaryMenu.get()!!.animateTranslationY(300f, 400, alpha = 0f)

            cvSubmenuInputsContainer.get()!!.visibility = View.GONE
            secondaryMenu.get()!!.visibility = View.GONE

            mainMenu.get()!!.getChildAt(lastFocusedMainCardsIndex).requestFocus()
            mainMenu.get()!!.animateTranslationY(0f, 400)
        }

//        mainMenu.get()!!.animateTranslationY(if (!isTop) 0f else 220f, 400)
    }

    private fun presentPictureModeSettings() {
        val settingsValues = arrayOf(
                Pair(R.string.brightness, pictureSettings.get()!!.brightness),
                Pair(R.string.contrast, pictureSettings.get()!!.contrast),
                Pair(R.string.saturation, pictureSettings.get()!!.saturation),
                Pair(R.string.temperature_des, pictureSettings.get()!!.temperature),
                Pair(R.string.backlight, pictureSettings.get()!!.backlight)
        )

        var indexOfSelectedPickerItem = 0
        pbSubmenuSettingProgress.get()!!.progress = settingsValues[indexOfSelectedPickerItem].second
        tvSubmenuSettingValue.get()!!.text = settingsValues[indexOfSelectedPickerItem].second.toString()
        npSubmenuSettingsItems.apply {
            get()!!.maxValue = 0
            get()!!.displayedValues = settingsValues.map { getString(it.first) }.toTypedArray()
            get()!!.maxValue = settingsValues.size - 1
            get()!!.minValue = 0
        }

        npSubmenuSettingsItems.get()!!.setOnValueChangedListener { _, _, newValue ->
            changeSubmenuSettingsPictureItem(newValue, settingsValues)
            indexOfSelectedPickerItem = newValue
        }

        npSubmenuSettingsItems.get()!!.setOnKeyListener { _, keyCode, event ->
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
                Pair(R.string.sound_height, pictureSettings.get()!!.getTrebleLevel(this)),
                Pair(R.string.sound_low, pictureSettings.get()!!.getBassLevel(this)),
                Pair(R.string.sound_balance, pictureSettings.get()!!.getBalanceLevel(this)),
                Pair(R.string.sound_height, pictureSettings.get()!!.getTrebleLevel(this)),
                Pair(R.string.sound_low, pictureSettings.get()!!.getBassLevel(this)),
                Pair(R.string.sound_balance, pictureSettings.get()!!.getBalanceLevel(this))
        )

        var indexOfSelectedPickerItem = 0
        pbSubmenuSettingProgress.get()!!.progress = settingsValues[indexOfSelectedPickerItem].second
        tvSubmenuSettingValue.get()!!.text = settingsValues[indexOfSelectedPickerItem].second.toString()
        npSubmenuSettingsItems.apply {
            get()!!.maxValue = 0
            get()!!.displayedValues = settingsValues.map { getString(it.first) }.toTypedArray()
            get()!!.maxValue = settingsValues.size - 1
            get()!!.minValue = 0
        }

        npSubmenuSettingsItems.get()!!.setOnValueChangedListener { _, _, newValue ->
            changeSubmenuSettingsSoundItem(settingsValues, newValue)
            indexOfSelectedPickerItem = newValue
        }

        npSubmenuSettingsItems.get()!!.setOnKeyListener { _, keyCode, event ->
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
            pbSubmenuSettingProgress.get()!!.visibility = View.GONE
            ivSubmenuSettingPictureTemperatureBar.get()!!.visibility = View.VISIBLE
            viewSubmenuSettingPictureTemperatureSelector.get()!!.visibility = View.VISIBLE

            val newTempBarText = when(pictureSettings.get()!!.temperature) {
                PICTURE_TEMPERATURE_MODE_NORMAL -> R.string.temperature_mode_normal
                PICTURE_TEMPERATURE_MODE_WARM -> R.string.temperature_mode_warm
                PICTURE_TEMPERATURE_MODE_VERY_WARM -> R.string.temperature_mode_very_warm
                PICTURE_TEMPERATURE_MODE_COLD -> R.string.temperature_mode_cold
                PICTURE_TEMPERATURE_MODE_VERY_COLD -> R.string.temperature_mode_very_cold
                else -> R.string.temperature_mode_normal
            }

            tvSubmenuSettingValue.get()!!.setText(newTempBarText)
            return
        }

        ivSubmenuSettingPictureTemperatureBar.get()!!.visibility = View.GONE
        viewSubmenuSettingPictureTemperatureSelector.get()!!.visibility = View.GONE
        tvSubmenuSettingValue.get()!!.text = settingsValues[newItemValue].second.toString()
        pbSubmenuSettingProgress.apply {
            get()!!.visibility = View.VISIBLE
            get()!!.max = 100
            get()!!.progressDrawable = ContextCompat.getDrawable(baseContext, R.drawable.gradient_progress)
            get()!!.progress = settingsValues[newItemValue].second
        }
    }

    private fun changeSubmenuSettingsPictureItemValue(indexOfSelectedPickerItem: Int, settingsValues: Array<Pair<Int, Int>>, isIncrementOperation: Boolean) {
        if (indexOfSelectedPickerItem == PICKER_INDEX_PICTURE_TEMPERATURE) {
            pictureSettings.get()!!.temperature = when(pictureSettings.get()!!.temperature) {
                PICTURE_TEMPERATURE_MODE_NORMAL -> if (isIncrementOperation) PICTURE_TEMPERATURE_MODE_WARM else PICTURE_TEMPERATURE_MODE_COLD
                PICTURE_TEMPERATURE_MODE_WARM -> if (isIncrementOperation) PICTURE_TEMPERATURE_MODE_VERY_WARM else PICTURE_TEMPERATURE_MODE_NORMAL
                PICTURE_TEMPERATURE_MODE_VERY_WARM -> if (isIncrementOperation) return else PICTURE_TEMPERATURE_MODE_WARM
                PICTURE_TEMPERATURE_MODE_COLD -> if (isIncrementOperation) PICTURE_TEMPERATURE_MODE_NORMAL else PICTURE_TEMPERATURE_MODE_VERY_COLD
                PICTURE_TEMPERATURE_MODE_VERY_COLD -> if (isIncrementOperation) PICTURE_TEMPERATURE_MODE_COLD else return
                else -> PICTURE_TEMPERATURE_MODE_NORMAL
            }

            val newTempBarTransX = when(pictureSettings.get()!!.temperature) {
                PICTURE_TEMPERATURE_MODE_NORMAL -> 0f
                PICTURE_TEMPERATURE_MODE_WARM -> pictureTemperatureBarBlockSize
                PICTURE_TEMPERATURE_MODE_VERY_WARM -> pictureTemperatureBarBlockSize * 2
                PICTURE_TEMPERATURE_MODE_COLD -> pictureTemperatureBarBlockSize * -1
                PICTURE_TEMPERATURE_MODE_VERY_COLD -> pictureTemperatureBarBlockSize * -2
                else -> 0f
            }

            val newTempBarText = when(pictureSettings.get()!!.temperature) {
                PICTURE_TEMPERATURE_MODE_NORMAL -> R.string.temperature_mode_normal
                PICTURE_TEMPERATURE_MODE_WARM -> R.string.temperature_mode_warm
                PICTURE_TEMPERATURE_MODE_VERY_WARM -> R.string.temperature_mode_very_warm
                PICTURE_TEMPERATURE_MODE_COLD -> R.string.temperature_mode_cold
                PICTURE_TEMPERATURE_MODE_VERY_COLD -> R.string.temperature_mode_very_cold
                else -> R.string.temperature_mode_normal
            }

            viewSubmenuSettingPictureTemperatureSelector.get()!!.animateTranslationX(newTempBarTransX, 150)
            tvSubmenuSettingValue.get()!!.setText(newTempBarText)
            return
        }

        if (isIncrementOperation) {
            pbSubmenuSettingProgress.get()!!.progress++
        } else {
            pbSubmenuSettingProgress.get()!!.progress--
        }

        tvSubmenuSettingValue.get()!!.text = pbSubmenuSettingProgress.get()!!.progress.toString()
        settingsValues[indexOfSelectedPickerItem] = settingsValues[indexOfSelectedPickerItem].copy(second = pbSubmenuSettingProgress.get()!!.progress)

        when(indexOfSelectedPickerItem) {
            PICKER_INDEX_PICTURE_BRIGHTNESS -> pictureSettings.get()!!.brightness = pbSubmenuSettingProgress.get()!!.progress
            PICKER_INDEX_PICTURE_CONTRAST -> pictureSettings.get()!!.contrast = pbSubmenuSettingProgress.get()!!.progress
            PICKER_INDEX_PICTURE_SATURATION -> pictureSettings.get()!!.saturation = pbSubmenuSettingProgress.get()!!.progress
            PICKER_INDEX_PICTURE_BACKLIGHT -> pictureSettings.get()!!.setBacklight(pbSubmenuSettingProgress.get()!!.progress, this)
        }

        return
    }

    private fun submenuSettingsPictureBackClick() {
        setModeUp(false)
        pbSubmenuSettingProgress.get()!!.visibility = View.VISIBLE
        ivSubmenuSettingPictureTemperatureBar.get()!!.visibility = View.GONE
        viewSubmenuSettingPictureTemperatureSelector.get()!!.visibility = View.GONE
    }

    private fun changeSubmenuSettingsSoundItem(settingsValues: Array<Pair<Int, Int>>, newValue: Int) {
        if (newValue == PICKER_INDEX_SOUND_BALANCE_1 || newValue == PICKER_INDEX_SOUND_BALANCE_2) {
            pbSubmenuSettingProgress.get()!!.visibility = View.GONE
            ivSubmenuSettingSoundBalanceBar.get()!!.visibility = View.VISIBLE
            viewSubmenuSettingSoundBalanceSelector.get()!!.visibility = View.VISIBLE
            tvSoundBalanceTitleLeft.get()!!.visibility = View.VISIBLE
            tvSoundBalanceTitleRight.get()!!.visibility = View.VISIBLE

            val balanceLevel = pictureSettings.get()!!.getBalanceLevel(baseContext)
            val balancePercentValue = abs(balanceLevel - 50) / 50f * 100
            tvSubmenuSettingValue.get()!!.text = when {
                balanceLevel > 50 -> getString(R.string.sound_balance_right_value, balancePercentValue.roundToInt())
                balanceLevel < 50 -> getString(R.string.sound_balance_left_value, balancePercentValue.roundToInt())
                else -> ""
            }

            return
        }

        ivSubmenuSettingSoundBalanceBar.get()!!.visibility = View.GONE
        viewSubmenuSettingSoundBalanceSelector.get()!!.visibility = View.GONE
        tvSoundBalanceTitleLeft.get()!!.visibility = View.GONE
        tvSoundBalanceTitleRight.get()!!.visibility = View.GONE

        tvSubmenuSettingValue.get()!!.text = settingsValues[newValue].second.toString()
        pbSubmenuSettingProgress.apply {
            get()!!.visibility = View.VISIBLE
            get()!!.max = 100
            get()!!.progressDrawable = ContextCompat.getDrawable(baseContext, R.drawable.gradient_progress)
            get()!!.progress = settingsValues[newValue].second
        }
    }

    private fun changeSubmenuSettingsSoundItemValue(indexOfSelectedPickerItem: Int, settingsValues: Array<Pair<Int, Int>>, isIncrementOperation: Boolean) {
        if (indexOfSelectedPickerItem == PICKER_INDEX_SOUND_BALANCE_1 || indexOfSelectedPickerItem == PICKER_INDEX_SOUND_BALANCE_2) {
            var balanceLevel = pictureSettings.get()!!.getBalanceLevel(baseContext)
            if (isIncrementOperation) {
                if (balanceLevel == 100) { return }
                balanceLevel++
                pictureSettings.get()!!.setBalanceLevel(baseContext, balanceLevel)
            } else {
                if (balanceLevel == 0) { return }
                balanceLevel--
                pictureSettings.get()!!.setBalanceLevel(baseContext, balanceLevel)
            }

            val balancePercentValue = abs(balanceLevel - 50) / 50f * 100
            tvSubmenuSettingValue.get()!!.text = when {
                balanceLevel > 50 -> getString(R.string.sound_balance_right_value, balancePercentValue.roundToInt())
                balanceLevel < 50 -> getString(R.string.sound_balance_left_value, balancePercentValue.roundToInt())
                else -> ""
            }

            viewSubmenuSettingSoundBalanceSelector.get()!!.translationX = when (balanceLevel) {
                50 -> 0f
                else -> soundBarSize / 2 * ((balanceLevel - 50).toFloat() / 50f)
            }

            return
        }

        if (isIncrementOperation) {
            pbSubmenuSettingProgress.get()!!.progress++
        } else {
            pbSubmenuSettingProgress.get()!!.progress--
        }

        tvSubmenuSettingValue.get()!!.text = pbSubmenuSettingProgress.get()!!.progress.toString()

        when(indexOfSelectedPickerItem) {
            PICKER_INDEX_SOUND_TREBLE_1, PICKER_INDEX_SOUND_TREBLE_2 -> {
                pictureSettings.get()!!.setTrebleLevel(this, pbSubmenuSettingProgress.get()!!.progress)
                settingsValues[PICKER_INDEX_SOUND_TREBLE_1] = settingsValues[PICKER_INDEX_SOUND_TREBLE_1].copy(second = pbSubmenuSettingProgress.get()!!.progress)
                settingsValues[PICKER_INDEX_SOUND_TREBLE_2] = settingsValues[PICKER_INDEX_SOUND_TREBLE_2].copy(second = pbSubmenuSettingProgress.get()!!.progress)
            }
            PICKER_INDEX_SOUND_BASS_1, PICKER_INDEX_SOUND_BASS_2 -> {
                pictureSettings.get()!!.setBassLevel(this, pbSubmenuSettingProgress.get()!!.progress)
                settingsValues[PICKER_INDEX_SOUND_BASS_1] = settingsValues[PICKER_INDEX_SOUND_BASS_1].copy(second = pbSubmenuSettingProgress.get()!!.progress)
                settingsValues[PICKER_INDEX_SOUND_BASS_2] = settingsValues[PICKER_INDEX_SOUND_BASS_2].copy(second = pbSubmenuSettingProgress.get()!!.progress)
            }
        }
    }

    private fun submenuSettingsSoundBackClick() {
        setModeUp(false)
        pbSubmenuSettingProgress.get()!!.visibility = View.VISIBLE
        ivSubmenuSettingSoundBalanceBar.get()!!.visibility = View.GONE
        viewSubmenuSettingSoundBalanceSelector.get()!!.visibility = View.GONE
        tvSoundBalanceTitleLeft.get()!!.visibility = View.GONE
        tvSoundBalanceTitleRight.get()!!.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        sleepTimer.removeCallbacks(updateSleepTimeCallback)
        typingTimer.removeCallbacks(channelTypingTimeout)
        windowManager.get()!!.removeView(generalView.get()!!)
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