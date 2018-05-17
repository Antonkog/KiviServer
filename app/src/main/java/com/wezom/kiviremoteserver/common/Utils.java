package com.wezom.kiviremoteserver.common;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import timber.log.Timber;

public class Utils {
    private static Method isStreamMute;

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = VectorDrawableCompat.create(context.getResources(), drawableId, null);
        Bitmap bitmap = null;

        if (drawable != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                drawable = (DrawableCompat.wrap(drawable)).mutate();
            }

            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }

        return bitmap;
    }

    public static boolean getMuteStatus(AudioManager audioManager) {
        boolean isMuted = false;
        if (audioManager == null)
            return false;

        try {
            if (isStreamMute == null)
                isStreamMute = AudioManager.class.getMethod("isStreamMute", int.class);

            isMuted = (Boolean) isStreamMute.invoke(audioManager, AudioManager.STREAM_MUSIC);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            Timber.e(e);
        }

        return isMuted;
    }
}
