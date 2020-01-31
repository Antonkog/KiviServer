package com.wezom.kiviremoteserver.service.aspect.aspect_v2.data

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.wezom.kiviremoteserver.R
import com.wezom.kiviremoteserver.service.aspect.aspect_v2.data.AspectMenuItem.Companion.TYPE_INPUTS
import com.wezom.kiviremoteserver.service.aspect.aspect_v2.data.AspectMenuItem.Companion.TYPE_KEYBOARD
import com.wezom.kiviremoteserver.service.aspect.aspect_v2.data.AspectMenuItem.Companion.TYPE_PICTURE
import com.wezom.kiviremoteserver.service.aspect.aspect_v2.data.AspectMenuItem.Companion.TYPE_RATIO
import com.wezom.kiviremoteserver.service.aspect.aspect_v2.data.AspectMenuItem.Companion.TYPE_SETTINGS
import com.wezom.kiviremoteserver.service.aspect.aspect_v2.data.AspectMenuItem.Companion.TYPE_SOUND
import com.wezom.kiviremoteserver.service.aspect.aspect_v2.data.AspectMenuItem.Companion.TYPE_TIMER
import com.wezom.kiviremoteserver.service.aspect.items.IFLMItems
import com.wezom.kiviremoteserver.service.aspect.items.TimerValues
import wezom.kiviremoteserver.environment.bridge.driver_set.PictureMode
import wezom.kiviremoteserver.environment.bridge.driver_set.Ratio
import wezom.kiviremoteserver.environment.bridge.driver_set.SoundValues

data class AspectMenuItem(
        val type: Int,
        @DrawableRes val image: Int,
        @StringRes val title: Int,
        @StringRes val subTitle: Int = 0,
        val values: List<IFLMItems> = listOf(),
        val showActionsView: Boolean = false
) {

    companion object {
        const val TYPE_INPUTS = 0
        const val TYPE_KEYBOARD = 1
        const val TYPE_PICTURE = 2
        const val TYPE_RATIO = 3
        const val TYPE_SETTINGS = 4
        const val TYPE_SOUND = 5
        const val TYPE_TIMER = 6
    }

}

object AspectMenuItems {
    private val inputsData = AspectMenuItem(TYPE_INPUTS, R.drawable.ic_hdmi_icon_copy, R.string.inputs, R.string.empty_str, showActionsView = true)
    private val numericData = AspectMenuItem(TYPE_KEYBOARD, R.drawable.ic_numeric_icon, R.string.keyboard, R.string.keyboard_desk)
    private val pictureData = AspectMenuItem(TYPE_PICTURE, R.drawable.ic_group_3_copy_6, R.string.picture, R.string.atv, PictureMode.getModes(), showActionsView = true)
    private val ratioData = AspectMenuItem(TYPE_RATIO,R.drawable.ic_screen_orientation_icon, R.string.ratio, R.string.hdmi, Ratio.getRatios())
    private val settingsData = AspectMenuItem(TYPE_SETTINGS, R.drawable.ic_settings_icon, R.string.settings, R.string.settings_desk)
    private val soundData = AspectMenuItem(TYPE_SOUND, R.drawable.ic_sound_icon, R.string.sound, R.string.atv, SoundValues.getModes(), showActionsView = true)
    private val timerData = AspectMenuItem(TYPE_TIMER, R.drawable.ic_clock_icon, R.string.sleep, R.string.sleep, listOf(
            TimerValues(R.string.t0, 0),
            TimerValues(R.string.t1, 15),
            TimerValues(R.string.t2, 30),
            TimerValues(R.string.t3, 60),
            TimerValues(R.string.t4, 120),
            TimerValues(R.string.t5, 180)
    ))

    //val allData = listOf(pictureData, soundData, inputsData, ratioData, timerData, numericData,  settingsData)
    val allData = mapOf(TYPE_PICTURE to pictureData, TYPE_SOUND to soundData, TYPE_INPUTS to inputsData, TYPE_RATIO to ratioData, TYPE_TIMER to timerData, TYPE_KEYBOARD to numericData, TYPE_SETTINGS to settingsData)
}

const val PICTURE_TEMPERATURE_MODE_NORMAL = 1
const val PICTURE_TEMPERATURE_MODE_WARM = 2
const val PICTURE_TEMPERATURE_MODE_VERY_WARM = 3
const val PICTURE_TEMPERATURE_MODE_COLD = 4
const val PICTURE_TEMPERATURE_MODE_VERY_COLD = 5