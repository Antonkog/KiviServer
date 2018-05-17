package com.wezom.kiviremoteserver.common

import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import timber.log.Timber


object MotionRelay {
    @JvmField
    val UPDATE_CURSOR_POSITION = 0

    @JvmField
    val LEFT_CLICK = 2

    val relay: Relay<CursorMotionEvent> = PublishRelay.create<CursorMotionEvent>().also { it.doOnError(Timber::e) }

    data class CursorMotionEvent @JvmOverloads constructor(val type: Int, val x: Float = 0f, val y: Float = 0f)
}