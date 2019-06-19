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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wezom.kiviremoteserver.net.server.model.Channel;
import com.wezom.kiviremoteserver.net.server.model.LauncherBasedData;
import com.wezom.kiviremoteserver.net.server.model.Recommendation;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

import static android.content.Context.UI_MODE_SERVICE;

/**
 * Created by andre on 02.06.2017.
 */

public class DeviceUtils {
    private DeviceUtils() {
    }

    private static final List<String> whiteListSystemApps = new ArrayList<>();
    private static final List<ApplicationInfo> userApps = new ArrayList<>();
    private static final List<Channel> channels = new ArrayList<>();
    private static final List<Recommendation> recommendations = new ArrayList<>();
    private static final List<Recommendation> favourites = new ArrayList<>();

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

    public static List<Recommendation> getRecommendations(Context context) {
        recommendations.clear();
        for (LauncherBasedData data : readData(new TypeToken<ArrayList<Recommendation>>() {
        }.getType(), LauncherBasedData.TYPE.RECOMMENDATION, context)) {
            recommendations.add((Recommendation) data);
        }
        return recommendations;
    }

    public static List<Recommendation> getFavourites(Context context) {
        favourites.clear();
        for (LauncherBasedData data : readData(new TypeToken<ArrayList<Recommendation>>() {
        }.getType(), LauncherBasedData.TYPE.FAVOURITE, context)) {
            favourites.add((Recommendation) data);
        }
        return favourites;
    }

    public static List<Channel> getChannels(Context context) {
        channels.clear();
        for (LauncherBasedData data : readData(new TypeToken<ArrayList<Channel>>() {
        }.getType(), LauncherBasedData.TYPE.CHANNEL, context)) {
            channels.add((Channel) data);
        }
        return channels;
    }

    private static List<LauncherBasedData> readData(Type typeOfT,LauncherBasedData.TYPE type,  Context context) {
        SharedPreferences preferences = getLauncherPreference(type, context);

        if (preferences != null) {
            try {
                String value = preferences.getString(Constants.LAUNCHER_PREF_KEY, "");
                return new Gson().fromJson(value, typeOfT);
            }catch (Exception e ){
                Timber.e("Can't get launcher based from gson  " + type.name());
            }
        } else {
            Timber.e("Can't get launcher based preference  preferences = null " + type.name());
        }
        return new ArrayList<>();
    }


    private static SharedPreferences getLauncherPreference(LauncherBasedData.TYPE type, Context context) {
        SharedPreferences dataprefs = null;
        try {
            Context myContext = context.createPackageContext(Constants.LAUNCHER_PACKAGE, Context.MODE_PRIVATE);
            switch (type) {
                case RECOMMENDATION:
                    dataprefs = myContext.getSharedPreferences(Constants.PREFERENCE_CATEGORY + Constants.RECOMMENDATION_MANAGER, Context.MODE_PRIVATE);
                    break;
                case CHANNEL:
                    dataprefs = myContext.getSharedPreferences(Constants.PREFERENCE_CATEGORY + Constants.CHANNEL_MANAGER, Context.MODE_PRIVATE);
                    break;
                case FAVOURITE:
                    dataprefs = myContext.getSharedPreferences(Constants.PREFERENCE_CATEGORY + Constants.FAVORITES_MANAGER, Context.MODE_PRIVATE);
                    break;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(" launcher based data error, type: " + type.name() + " " + e);
        }
        return dataprefs;
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
        Set<String> apps;
        try {
            Context myContext = context.createPackageContext("com.kivi.launcher",
                    Context.MODE_PRIVATE);

            SharedPreferences testPrefs = myContext.getSharedPreferences
                    ("kivi.launcher", Context.MODE_PRIVATE);
            apps = testPrefs.getStringSet("white_list", null);
            if (apps != null) {
                whiteListSystemApps.clear();
                whiteListSystemApps.addAll(apps);
            } else {
                Timber.e("getWhiteList empty");
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
