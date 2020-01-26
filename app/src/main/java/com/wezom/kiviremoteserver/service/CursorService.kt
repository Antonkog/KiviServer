package com.wezom.kiviremoteserver.service

import android.app.Instrumentation
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import com.wezom.kiviremoteserver.App
import com.wezom.kiviremoteserver.R
import com.wezom.kiviremoteserver.common.LongTapMenuProvider
import com.wezom.kiviremoteserver.common.MotionRelay
import com.wezom.kiviremoteserver.common.MotionRelay.LEFT_CLICK
import com.wezom.kiviremoteserver.common.MotionRelay.LONG_TAP_DOWN
import com.wezom.kiviremoteserver.common.MotionRelay.LONG_TAP_UP
import com.wezom.kiviremoteserver.common.MotionRelay.UPDATE_CURSOR_POSITION
import com.wezom.kiviremoteserver.common.extensions.toPx
import com.wezom.kiviremoteserver.ui.views.OverlayView
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.longToast
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class CursorService : Service() {

    @Inject
    lateinit var longTapMenuProvider: LongTapMenuProvider

    private lateinit var overlayView: OverlayView
    private lateinit var windowManager: WindowManager

    private val binder = LocalBinder()

    @Suppress("DEPRECATION")
    private val cursorLayout = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,//TYPE_SYSTEM_ALERT,//TYPE_SYSTEM_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    FLAG_NOT_TOUCH_MODAL, //will cover status bar as well!!!
            PixelFormat.TRANSLUCENT).apply { gravity = Gravity.START or Gravity.TOP }

    private var hideTimer: Disposable? = null
    private var longHideTimer: Disposable? = null
    private val instrumentation = Instrumentation()

    private var isAdded: Boolean = false

    private var x = 200f
    private var y = 200f

    private var maxX: Int = 0
    private var maxY: Int = 0

    override fun onBind(intent: Intent?): IBinder = binder

    private val motionDisposable = MotionRelay.relay.subscribe({
        when (it.type) {
            UPDATE_CURSOR_POSITION -> update(it.x, it.y)
            LEFT_CLICK -> leftButtonClick()
            LONG_TAP_DOWN -> longButtonClick(false, it.x, it.y)
            LONG_TAP_UP -> longButtonClick(true, it.x, it.y)
            else -> Timber.e("Unknown action")
        }
    }, {})

    inner class LocalBinder : Binder() {
        fun getService() = this@CursorService
    }

    override fun onCreate() {
        super.onCreate()
        App.getApplicationComponent().inject(this)
        overlayView = OverlayView(this, longTapMenuProvider)

        val displayMetrics = DisplayMetrics()
        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay?.getMetrics(displayMetrics)
            with(displayMetrics) {
                maxX = widthPixels
                maxY = heightPixels
            }
            windowManager.addView(overlayView, cursorLayout)
            isAdded = true
        } catch (e: Throwable) {
            Timber.e(e, e.message)
            longToast(R.string.overlay_was_denied)
            isAdded = false
        }
    }

    private fun showCursor() {
        showCursor(true)
        hideTimer?.takeUnless { it.isDisposed }?.dispose()
        hideTimer = Observable.timer(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ _ -> showCursor(false) }) { e -> Timber.e(e, e.message) }
    }

    private fun update(x1: Float, y1: Float) {
        Timber.e("update x y on move")
        showCursor()
        onMouseMove(x1.toInt(), y1.toInt())
    }

    private fun showCursor(status: Boolean) = overlayView.run {
        showCursor(status)
        postInvalidate()
    }

    override fun onDestroy() {
        if (isAdded) windowManager.removeView(overlayView)
        motionDisposable.takeUnless { it.isDisposed }?.dispose()
        super.onDestroy()
    }

    private fun leftButtonClick() {
        Single.fromCallable<Int>(::executeLeftClick)
                .subscribeOn(Schedulers.computation())
                .subscribe()
    }

    private fun longButtonClick(tapUp: Boolean, x: Float, y: Float) {
        longHideTimer?.takeUnless { it.isDisposed }?.dispose()
        longHideTimer = Observable.timer(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ _ ->
                    overlayView.run {
                        turnOffLongTap()
                    }
                }) { e -> Timber.e(e, e.message) }

        overlayView.run {
            longTap(tapUp, x, y)
            postInvalidate()
        }

        try {
            windowManager.updateViewLayout(overlayView, cursorLayout)
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Illegal argument exception: ${e.message}. Are you sure that you have SYSTEM_ALERT_WINDOW permission?")
        }
    }

    private fun executeLeftClick(): Int {
        try {
            instrumentation.sendPointerSync(MotionEvent.obtain(
                    SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_DOWN, x, y, 0))
            instrumentation.sendPointerSync(MotionEvent.obtain(
                    SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_UP, x, y, 0))
        } catch (e: SecurityException) {
            Timber.e(e)
        }
        return 0
    }

    private fun onMouseMove(dx: Int, dy: Int) {
        val tx = dx.toPx
        val ty = dy.toPx

        x += tx
        y += ty

        //SET DISPLAY BORDERS
        if (maxY <= y) {
            y = maxY.toFloat()
        }
        if (maxX <= x) {
            x = maxX.toFloat()
        }

        if (x <= 0) {
            x = 0f
        }

        if (y <= 0) {
            y = 0f
        }

        try {
            windowManager.updateViewLayout(overlayView, cursorLayout)
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Illegal argument exception: ${e.message}. Are you sure that you have SYSTEM_ALERT_WINDOW permission?")
        }

        overlayView.run {
            update(this@CursorService.x, this@CursorService.y)
            postInvalidate()
        }
    }
}