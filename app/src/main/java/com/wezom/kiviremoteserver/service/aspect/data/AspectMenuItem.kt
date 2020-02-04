package com.wezom.kiviremoteserver.service.aspect.data

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.wezom.kiviremoteserver.service.aspect.values.IFLMItems

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

const val PICTURE_TEMPERATURE_MODE_NORMAL = 1
const val PICTURE_TEMPERATURE_MODE_WARM = 2
const val PICTURE_TEMPERATURE_MODE_VERY_WARM = 3
const val PICTURE_TEMPERATURE_MODE_COLD = 4
const val PICTURE_TEMPERATURE_MODE_VERY_COLD = 5