package com.wezom.kiviremoteserver.common

import android.content.Context
import com.wezom.kiviremoteserver.R
import com.wezom.kiviremoteserver.di.qualifiers.ApplicationContext
import com.wezom.kiviremoteserver.net.server.model.LongTapAction
import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper
import wezom.kiviremoteserver.environment.bridge.BridgeInputs
import java.util.*
import javax.inject.Inject

//
// Created by Antonio on 2019-12-17.
// email: akogan777@gmail.com
// Copyright (c) 2019 KIVI. All rights reserved.
//

class LongTapMenuProvider @Inject constructor(@ApplicationContext val context: Context, val cache: KiviCache) {

    private lateinit var longTapActions: LinkedList<LongTapAction>

    private val bridgeInputs = BridgeInputs()

    fun getAllActions(): LinkedList<LongTapAction> {
        longTapActions = LinkedList()
        longTapActions.add(LongTapAction("Settings", R.string.lt_settings, R.drawable.ic_lt_settings, null))
        longTapActions.add(LongTapAction("HDMI_settings", R.string.lt_hdmi_settings, R.drawable.ic_lt_settings, null))
        longTapActions.add(LongTapAction("Q_settings", R.string.lt_q_settings, R.drawable.ic_lt_quick_set, null))
        longTapActions.add(LongTapAction("Home", R.string.lt_home, R.drawable.ic_lt_home, null))
        longTapActions.add(LongTapAction("Back", R.string.lt_back, R.drawable.ic_lt_back, null))
        longTapActions.add(LongTapAction("Channel_list", R.string.lt_channels_list, R.drawable.ic_lt_channels, null))
        longTapActions.add(LongTapAction("Film_catalogue", R.string.lt_film_catalogue, R.drawable.ic_lt_movie, null))
        longTapActions.add(LongTapAction("widgets", R.string.lt_widgets, R.drawable.ic_lt_widgets, null))
        return longTapActions
    }

    fun getCurrentActions(): LinkedList<LongTapAction> {
        longTapActions = LinkedList()
        when (bridgeInputs.currentTvInputSource) {
            InputSourceHelper.INPUT_PORT.INPUT_SOURCE_HDMI.id,
            InputSourceHelper.INPUT_PORT.INPUT_SOURCE_HDMI2.id,
            InputSourceHelper.INPUT_PORT.INPUT_SOURCE_HDMI3.id,
            InputSourceHelper.INPUT_PORT.INPUT_SOURCE_HDMI4.id -> {
                longTapActions.add(LongTapAction("HDMI_settings", R.string.lt_hdmi_settings, R.drawable.ic_lt_settings, null))
                longTapActions.add(LongTapAction("Q_settings", R.string.lt_q_settings, R.drawable.ic_lt_quick_set, null))
                longTapActions.add(LongTapAction("Home", R.string.lt_home, R.drawable.ic_lt_home, null))
                longTapActions.add(LongTapAction("Back", R.string.lt_back, R.drawable.ic_lt_back, null))
            }

            InputSourceHelper.INPUT_PORT.INPUT_SOURCE_ATV.id,
            InputSourceHelper.INPUT_PORT.INPUT_SOURCE_DVBS.id,
            InputSourceHelper.INPUT_PORT.INPUT_SOURCE_DTV.id,
            InputSourceHelper.INPUT_PORT.INPUT_SOURCE_DVBC.id -> {
                longTapActions.add(LongTapAction("Settings", R.string.lt_settings, R.drawable.ic_lt_settings, null))
                longTapActions.add(LongTapAction("Q_settings", R.string.lt_q_settings, R.drawable.ic_lt_quick_set, null))
                longTapActions.add(LongTapAction("Home", R.string.lt_home, R.drawable.ic_lt_home, null))
                longTapActions.add(LongTapAction("Back", R.string.lt_back, R.drawable.ic_lt_back, null))
                longTapActions.add(LongTapAction("Channel_list", R.string.lt_channels_list, R.drawable.ic_lt_channels, null))
            }

            else -> {
                longTapActions.add(LongTapAction("Settings", R.string.lt_settings, R.drawable.ic_lt_settings, null))
                longTapActions.add(LongTapAction("Q_settings", R.string.lt_q_settings, R.drawable.ic_lt_quick_set, null))
                longTapActions.add(LongTapAction("Film_catalogue", R.string.lt_film_catalogue, R.drawable.ic_lt_movie, null))
                longTapActions.add(LongTapAction("widgets", R.string.lt_widgets, R.drawable.ic_lt_widgets, null))
            }
        }

//        longTapActions.forEach {
//            cache.put(it.actionId, Utils.getBitmapFromVectorDrawable(context, it.imgResId))
//        }
        return longTapActions
    }

}