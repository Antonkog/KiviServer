package com.wezom.kiviremoteserver.common;

import android.app.UiModeManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import timber.log.Timber;

import static android.content.Context.MODE_WORLD_READABLE;
import static android.content.Context.UI_MODE_SERVICE;

/**
 * Created by andre on 02.06.2017.
 */

public class DeviceUtils {
    private DeviceUtils() {
    }

    private static final List<String> whiteListSystemApps = new ArrayList<>();
    private static final List<ApplicationInfo> userApps = new ArrayList<>();

    public static String getDeviceName() {
        return Build.MODEL;
    }

    private static String capitalize(String s) {
        if (s.isEmpty()) {
            return "";
        }

        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static boolean isTvDevice(Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(UI_MODE_SERVICE);
        return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;

    }

    public static Enumeration<?> getAllSystemProperties() {
        return System.getProperties().propertyNames();
    }

    public static String getSystemName(Context context) {
        return android.os.Build.MODEL;
    }

    public static List<ApplicationInfo> getInstalledApplications(Context context, PackageManager packageManager) {
        userApps.clear();
        getWhiteList(context);

        for (ApplicationInfo app : packageManager.getInstalledApplications(PackageManager.GET_META_DATA)) {
            if (packageManager.getLaunchIntentForPackage(app.packageName) != null && isNotExcluded(app))
                // todo For testing purposes
//            if (packageManager.getLaunchIntentForPackage(app.packageName) != null)
                userApps.add(app);
        }

        return userApps;
    }

    private static boolean isNotExcluded(ApplicationInfo info) {
        if (info.packageName.equals("com.ua.mytrinity.tvplayer.kivitv"))
            return true;

        if (info.packageName.equals("com.wezom.kiviremoteserver"))
            return false;

        if (isSystemApp(info) && isInWhiteList(info.packageName))
            return true;

        if (!isSystemApp(info))
            return true;

        if (isSystemApp(info) && !isInWhiteList(info.packageName))
            return false;

        return false;
    }

    private static boolean isSystemApp(ApplicationInfo info) {
        return (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    private static boolean isInWhiteList(String packageName) {
        return whiteListSystemApps.contains(packageName);
    }

    private static List<String> getWhiteList(Context context) {
        int length;
        SharedPreferences sp;
        try {
            Context c = context.createPackageContext("com.bestv.ott", Context.CONTEXT_IGNORE_SECURITY);
            sp = c.getSharedPreferences("systemwhiltelist", MODE_WORLD_READABLE);
            length = sp.getInt("length", 0);
            whiteListSystemApps.clear();
            for (int i = 0; i < length; i++) {
                whiteListSystemApps.add(sp.getString("string" + i, null));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e, e.getMessage());
        }
        return whiteListSystemApps;
    }

    public static boolean isUserApp(ApplicationInfo info) {
        int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        return (info.flags & mask) == 0;
    }

    public static String getApplicationName(PackageManager packageManager, ApplicationInfo applicationInfo) {
        return applicationInfo.loadLabel(packageManager).toString();
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        final int width = !drawable.getBounds().isEmpty() ? drawable
                .getBounds().width() : drawable.getIntrinsicWidth();

        final int height = !drawable.getBounds().isEmpty() ? drawable
                .getBounds().height() : drawable.getIntrinsicHeight();

        final Bitmap bitmap = Bitmap.createBitmap(width <= 0 ? 1 : width,
                height <= 0 ? 1 : height, Bitmap.Config.ARGB_8888);

        Timber.d("Bitmap width - Height :", width + " : " + height);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
