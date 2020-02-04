package com.wezom.kiviremoteserver.service.aspect.data

import com.wezom.kiviremoteserver.R
import com.wezom.kiviremoteserver.service.aspect.values.TimerValues
import wezom.kiviremoteserver.environment.bridge.driver_set.PictureMode
import wezom.kiviremoteserver.environment.bridge.driver_set.Ratio
import wezom.kiviremoteserver.environment.bridge.driver_set.SoundValues

object AspectMenuItems {
    private val inputsData = AspectMenuItem(AspectMenuItem.TYPE_INPUTS, R.drawable.ic_hdmi_icon_copy, R.string.inputs, R.string.empty_str, showActionsView = true)
    private val numericData = AspectMenuItem(AspectMenuItem.TYPE_KEYBOARD, R.drawable.ic_numeric_icon, R.string.keyboard, R.string.keyboard_desk)
    private val pictureData = AspectMenuItem(AspectMenuItem.TYPE_PICTURE, R.drawable.ic_group_3_copy_6, R.string.picture, R.string.atv, PictureMode.getModes(), showActionsView = true)
    private val ratioData = AspectMenuItem(AspectMenuItem.TYPE_RATIO, R.drawable.ic_screen_orientation_icon, R.string.ratio, R.string.hdmi, Ratio.getRatios())
    private val settingsData = AspectMenuItem(AspectMenuItem.TYPE_SETTINGS, R.drawable.ic_settings_icon, R.string.settings, R.string.settings_desk)
    private val soundData = AspectMenuItem(AspectMenuItem.TYPE_SOUND, R.drawable.ic_sound_icon, R.string.sound, R.string.atv, SoundValues.getModes(), showActionsView = true)
    private val timerData = AspectMenuItem(AspectMenuItem.TYPE_TIMER, R.drawable.ic_clock_icon, R.string.sleep, R.string.sleep, listOf(
            TimerValues(R.string.t0, 0),
            TimerValues(R.string.t1, 15),
            TimerValues(R.string.t2, 30),
            TimerValues(R.string.t3, 60),
            TimerValues(R.string.t4, 120),
            TimerValues(R.string.t5, 180)
    ))

    //val allData = listOf(pictureData, soundData, inputsData, ratioData, timerData, numericData,  settingsData)
    val allData = mapOf(AspectMenuItem.TYPE_PICTURE to pictureData, AspectMenuItem.TYPE_SOUND to soundData, AspectMenuItem.TYPE_INPUTS to inputsData, AspectMenuItem.TYPE_RATIO to ratioData, AspectMenuItem.TYPE_TIMER to timerData, AspectMenuItem.TYPE_KEYBOARD to numericData, AspectMenuItem.TYPE_SETTINGS to settingsData)
}