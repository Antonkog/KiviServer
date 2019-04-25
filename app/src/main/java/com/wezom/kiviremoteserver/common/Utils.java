package com.wezom.kiviremoteserver.common;


import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Environment;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;

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


    public static boolean isServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public static File getLogFile() {
        return new File(Environment.getExternalStorageDirectory() + File.separator + Constants.LOG_FILE_PREFIX + Build.MODEL + Constants.LOG_FILE_EXTENSION);
    }


    public static void appendLog(String text) {
        System.out.println(text);
        Timber.i(text);
        File logFile = getLogFile();
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text + " " + calendar.getTime().toString()) ;
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
