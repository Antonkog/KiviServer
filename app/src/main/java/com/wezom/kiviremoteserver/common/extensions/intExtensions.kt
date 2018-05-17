@file:JvmName("IntUtils")

package com.wezom.kiviremoteserver.common.extensions

import android.content.res.Resources

val density = Resources.getSystem().displayMetrics.density

val Int.toPx: Int
    get() = (this * density).toInt()

val Int.toDp: Int
    get() = (this / density).toInt()