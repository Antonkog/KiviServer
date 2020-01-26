package com.wezom.kiviremoteserver.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import com.wezom.kiviremoteserver.common.LongTapMenuProvider
import com.wezom.kiviremoteserver.common.RxBus
import com.wezom.kiviremoteserver.common.Utils.getBitmapFromVectorDrawable
import com.wezom.kiviremoteserver.common.extensions.distance
import com.wezom.kiviremoteserver.common.extensions.toPx
import com.wezom.kiviremoteserver.net.server.model.LongTapAction
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


/**
 * Created by andre on 12.06.2017.
 */

class OverlayView(context: Context, val longTapMenuProvider: LongTapMenuProvider) : ViewGroup(context) {

    private var showCursor: Boolean = false
    private var logTapMode: Boolean = false

    private var longTapStartPos = Point(1920/2, 720/2)

    private var lx = 0f
    private var ly = 0f
    private val MAX_SCALE_FACTOR = 1.1f

    private var maxWidth = 1920
    private var maxHeight = 720

    private val textPaint = Paint()
    private val textPaintGray = Paint()
    private var lastLongTapAction: LongTapAction? = null

    private var animator: ValueAnimator? = null
    private var currentScaleFactor = 0f

    private val bitmapHolder = mutableMapOf<LongTapAction, Bitmap>()

    private val cursor: Bitmap? = getBitmapFromVectorDrawable(context, com.wezom.kiviremoteserver.R.drawable.ic_cursor)

    var activeAngle = 0.000

    fun update(nx: Float, ny: Float) {
        lx = nx
        ly = ny

        val difX = nx - longTapStartPos.x
        val dify = ny - longTapStartPos.y

        if (difX != 0f && dify != 0f) {
            activeAngle = atan2(dify.toDouble(), difX.toDouble())
//            Timber.e(" long move!!! x = $nx y = $ny  oldx ${longTapStartPos.x}  oldy ${longTapStartPos.y} angle is $activeAngle")
        }
    }

    fun showCursor(status: Boolean) {
        if (!logTapMode)
            showCursor = status
    }

    fun longTap(tapUp: Boolean, nx: Float, ny: Float) {
        if (tapUp) {
            longClickAction()
            turnOffLongTap()
            animator?.cancel()
        } else {
            startAnimation()
            showCursor(false)
            logTapMode = true
            activeAngle = 0.000
//            longTapStartPos = Point(nx.toInt(), ny.toInt())
        }
    }

    fun turnOffLongTap() {
        logTapMode = false
    }

    private fun longClickAction() {
        lastLongTapAction?.let {
//            Timber.e("Long tap action 1= " + lastLongTapAction.toString())
            RxBus.publish(LongTapAction(it.actionId, it.name, it.imgResId, it.imgUrl))        }
    }


    init {
        setBackgroundColor(Color.TRANSPARENT)

        val display = (getContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size = Point()
        display.getSize(size)
        maxWidth = size.x
        maxHeight = size.y

       longTapStartPos =  Point(maxWidth/2, maxHeight/2)

        longTapMenuProvider.getAllActions().forEach { action ->
            getBitmapFromVectorDrawable(context, action.imgResId).let {
                bitmapHolder[action] = it

            }
        }

        initPaints()
    }

    private fun initPaints() {
        textPaint.color = Color.WHITE
        textPaint.textSize = 30f
        textPaint.isAntiAlias = true


        textPaintGray.color = Color.GRAY
        textPaintGray.textSize = 30f
        textPaintGray.isAntiAlias = true
    }


    private fun startAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(1f, MAX_SCALE_FACTOR).apply {
            //            repeatCount = ValueAnimator.INFINITE
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { valueAnimator ->
                currentScaleFactor = valueAnimator.animatedValue as Float
                invalidate()
            }
        }
        animator?.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (maxHeight <= ly) {
            return
        }
        if (maxWidth <= lx) {
            return
        }
        if (showCursor) {
            canvas.drawBitmap(cursor!!, lx, ly, null)
        }
        if (logTapMode)
            drawMenu(canvas)
    }

    fun drawMenu(canvas: Canvas) {
        canvas.drawARGB(230, 0, 0, 0)
        val osX = maxWidth.toDouble() / 2
        val osY = maxHeight.toDouble() / 2
        val actions = longTapMenuProvider.getCurrentActions()

        val iconSize = Resources.getSystem().displayMetrics.density * 55
        val rad = iconSize * 2.1 //distance from center can be any  value
        val size = actions.size
        var angle = (2 * Math.PI / size)

        val bounds = Rect()

//        Timber.e(" angle = $angle rad = $rad")
        lastLongTapAction = null

        actions.forEachIndexed { index, action ->

            val currentAngle = angle * index - Math.PI / 2 //- getIconAngleOffset(iconSize/2f , rad)
//need to move angle left on icon_size/2
            val icX = (rad * cos(currentAngle)) + osX - iconSize / 2
            val icY = (rad * sin(currentAngle)) + osY - iconSize / 2

            textPaint.getTextBounds(action.getStringName(context), 0, action.getStringName(context).length, bounds)

            val width = bounds.width()
            val textOffset = (iconSize - width) / 2

//            Timber.e(" currentAngle $currentAngle  activeAngle $activeAngle difference ${currentAngle - activeAngle}")

            val iclineX = (rad * cos(activeAngle)) + osX - iconSize / 2
            val iclineY = (rad * sin(activeAngle)) + osY - iconSize / 2
            if (distance(icX, icY, iclineX, iclineY) < iconSize) {

                val moveFactor = (iconSize * currentScaleFactor - iconSize) / 2

                val rect = Rect((icX - moveFactor).toInt(), (icY - moveFactor).toInt(),
                        (icX + iconSize + moveFactor).toInt(),
                        (icY + iconSize + moveFactor).toInt())

                canvas.drawBitmap(bitmapHolder[action], null,
                        rect,
                        null)

                canvas.drawText(action.getStringName(context), icX.toFloat() + textOffset, icY.toFloat() + iconSize + 25.toPx, textPaint)
                lastLongTapAction = action
//                canvas.drawBitmap(bitmapScaledHolder[action], icX.toFloat(), icY.toFloat(), null)
            } else {
                canvas.drawBitmap(bitmapHolder[action], icX.toFloat(), icY.toFloat(), null)
                canvas.drawText(action.getStringName(context), icX.toFloat() + textOffset, icY.toFloat() + iconSize + 25.toPx, textPaintGray)
            }

//            canvas.drawLine(osX.toFloat(), osY.toFloat(), iclineX.toFloat() + iconSize / 2, iclineY.toFloat() + iconSize / 2, textPaint) //center todo remove
//            canvas.drawCircle(iclineX.toFloat() + iconSize / 2, iclineY.toFloat() + iconSize / 2,2f, textPaintGray)
//            Timber.e("position icon: angle = $currentAngle  x= $icX y $icY , index  = $index")
        }
    }

    override fun onLayout(arg0: Boolean, arg1: Int, arg2: Int, arg3: Int, arg4: Int) {}

    override fun onTouchEvent(event: MotionEvent): Boolean {
//        Timber.d("onTouchEvent %s  %s  %s  %s", event.y, event.x, event.action, event.actionMasked)
        super.onTouchEvent(event)
        return false
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
//        Timber.d("onInterceptTouchEvent %s %s  %s  %s", e.y, e.x, e.action, e.actionMasked)
        onTouchEvent(e)
        return false
    }
}
