package com.wezom.kiviremoteserver.service.aspect.aspect_v2.data

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.wezom.kiviremoteserver.R
import com.wezom.kiviremoteserver.service.aspect.items.IFLMItems
import com.wezom.kiviremoteserver.service.aspect.items.TimerValues
import wezom.kiviremoteserver.environment.bridge.driver_set.PictureMode
import wezom.kiviremoteserver.environment.bridge.driver_set.SoundValues

data class CardData(
        val type: Int,
        @DrawableRes val image: Int,
        @StringRes val title: Int,
        @StringRes val subTitle: Int = 0,
        val values: List<IFLMItems> = listOf()
)

object Cards {
    private val inputsData = CardData(0,R.drawable.ic_hdmi_icon_copy, R.string.inputs, R.string.hdmi)
    private val numericData = CardData(1,R.drawable.ic_numeric_icon, R.string.keyboard, R.string.keyboard_desk)
    private val pictureData = CardData(2,R.drawable.ic_group_3_copy_6, R.string.picture, R.string.atv, PictureMode.getModes())
    private val ratioData = CardData(3,R.drawable.ic_screen_orientation_icon, R.string.ratio, R.string.hdmi)
    private val settingsData = CardData(4,R.drawable.ic_settings_icon, R.string.settings, R.string.settings_desk)
    private val soundData = CardData(5, R.drawable.ic_sound_icon, R.string.sound, R.string.atv, SoundValues.getModes())
    private val timerData = CardData(6,R.drawable.ic_clock_icon, R.string.sleep, R.string.sleep, listOf(TimerValues(R.string.t1),
            TimerValues(R.string.t2), TimerValues(R.string.t3), TimerValues(R.string.t4), TimerValues(R.string.t5)))

    val allData = listOf(pictureData, soundData, inputsData, ratioData, timerData, numericData,  settingsData)
}