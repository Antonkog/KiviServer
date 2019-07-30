package com.wezom.kiviremoteserver.common.extensions

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.Toast
import com.wezom.kiviremoteserver.BuildConfig
import com.wezom.kiviremoteserver.common.Constants
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber
import java.io.ByteArrayOutputStream


fun dpToPx(context: Context, dps: Int) = Math.round(context.resources.displayMetrics.density * dps)

fun Context.toastOutsource(message: CharSequence) =
        if (BuildConfig.VERSION_NAME.toLowerCase().contains("test")) Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        else Timber.v("" + message)

private fun createBitmap(drawable: Drawable, width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun getIconBytes(context: Context, w: Int, h: Int, banner: Drawable?): ByteArray? {
    ByteArrayOutputStream().use { stream ->
        var iconBytes = byteArrayOf()
        if (banner != null) {
            val bitmap = createBitmap(banner, dpToPx(context, w), dpToPx(context, h))
            bitmap.compress(Bitmap.CompressFormat.PNG, 60, stream)
            iconBytes = stream.toByteArray()
        }
        return iconBytes
    }
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