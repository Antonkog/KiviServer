package com.wezom.kiviremoteserver.common.extensions

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import timber.log.Timber
import java.io.ByteArrayOutputStream
import kotlin.math.pow
import kotlin.math.sqrt


fun dpToPx(context: Context, dps: Int) = Math.round(context.resources.displayMetrics.density * dps)


private fun createBitmap(drawable: Drawable, width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun getIconBytes(context: Context, outWidth: Int, outHeight: Int, banner: Drawable): ByteArray? {
    ByteArrayOutputStream().use { stream ->
        var iconBytes = byteArrayOf()
        val w = if (banner.intrinsicWidth > 1) {
            banner.intrinsicWidth
        } else {
            outWidth
        }
        val h = if (banner.intrinsicHeight > 1) {
            banner.intrinsicHeight
        } else {
            outHeight
        }

        if (banner != null) {
            var bitmap = createBitmap(banner, w, h)
            val scaleFactor = bitmap.width / outWidth

            if (scaleFactor > 1) {
                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / scaleFactor, bitmap.height / scaleFactor, false)
            }

            bitmap.compress(Bitmap.CompressFormat.PNG, 60, stream)
            iconBytes = stream.toByteArray()
        }
        return iconBytes
    }
}


fun distance(x: Double, y: Double, endX : Double , endY : Double): Double {
    return sqrt((endX - x).pow(2.0) + (endY - y).pow(2.0))  //ERROR IN THIS LINE
}

fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }
    val width = if (!drawable.bounds.isEmpty)
        drawable.bounds.width()
    else drawable.intrinsicWidth

    val height = if (!drawable.bounds.isEmpty)
        drawable.bounds.height()
    else
        drawable.intrinsicHeight

    val bitmap = Bitmap.createBitmap(if (width <= 0) 1 else width,
            if (height <= 0) 1 else height, Bitmap.Config.ARGB_4444)

    Timber.d("Bitmap width - Height :", "$width : $height")
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}

fun decodeSampledBitmapFromResource(
        res: Resources,
        resId: Int,
        reqWidth: Int,
        reqHeight: Int
): Bitmap {
    // First decode with inJustDecodeBounds=true to check dimensions
    return BitmapFactory.Options().run {
        inJustDecodeBounds = true
        BitmapFactory.decodeResource(res, resId, this)

        // Calculate inSampleSize
        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        inJustDecodeBounds = false

        BitmapFactory.decodeResource(res, resId, this)
    }
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    // Raw height and width of image
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {

        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}