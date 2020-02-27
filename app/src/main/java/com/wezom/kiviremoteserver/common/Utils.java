package com.wezom.kiviremoteserver.common;


import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.wezom.kiviremoteserver.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;

import timber.log.Timber;

public class Utils {
    private static Method isStreamMute;
    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        return getBitmapFromVectorDrawable(context, drawableId, 1f);

    }
    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId, float scaleFactor) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        Bitmap bitmap = null;
        if (drawable != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                drawable = (DrawableCompat.wrap(drawable)).mutate();
            }

            bitmap = Bitmap.createBitmap( (int)(drawable.getIntrinsicWidth() * scaleFactor ) ,
                    (int)(drawable.getIntrinsicHeight()* scaleFactor), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }

        return bitmap;
    }

    public static Uri resourceToUri(Resources resources, int resID) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                resources.getResourcePackageName(resID) + '/' +
                resources.getResourceTypeName(resID) + '/' +
                resources.getResourceEntryName(resID) );
    }

   public static File saveBitmapToFile(File dir, String fileName, Bitmap bm,
                             Bitmap.CompressFormat format, int quality) {

        File imageFile = new File(dir,fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);

            bm.compress(format,quality,fos);

            fos.close();

            return imageFile;
        }
        catch (IOException e) {
            Log.e("app",e.getMessage());
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
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


    private final static String TAG = "DebugUtils";


    public static void appendLog(String text) {
        appendLog(TAG, text);
    }

    public static void appendLog(String tag, String text) {
        Log.e(tag, text);
        Timber.e(text);
        File logFile = getLogFile();
//        Timber.e("log file location " + logFile.getAbsolutePath());
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text + " " + calendar.getTime().toString());
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showDebugErrorMessage(Throwable throwable, Context context) {
        if (BuildConfig.DEBUG) {
            throwable.printStackTrace();
            Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void logV(String tag, String message) {
        if (tag != null && message != null && !message.isEmpty() && BuildConfig.DEBUG)
            Log.v(tag, message);
    }

    public static void logE(String tag, String message) {
        if (tag != null && message != null && !message.isEmpty()) {
            Log.e(tag, message);
            if (!BuildConfig.DEBUG)
                try {
                    Timber.e(tag, message);
                } catch (Exception e) {
                    Log.e("FirebaseCrash", e.getMessage());
                }
        }
    }
}
