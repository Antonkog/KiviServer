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
import android.support.annotation.RequiresApi;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wezom.kiviremoteserver.common.extensions.ViewExtensionsKt;
import com.wezom.kiviremoteserver.net.server.model.Channel;
import com.wezom.kiviremoteserver.net.server.model.LauncherBasedData;
import com.wezom.kiviremoteserver.net.server.model.PreviewCommonStructure;
import com.wezom.kiviremoteserver.net.server.model.Recommendation;
import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


import io.reactivex.Single;
import timber.log.Timber;

import static android.content.Context.UI_MODE_SERVICE;

/**
 * Created by andre on 02.06.2017.
 */

public class DeviceUtils {
    private DeviceUtils() {
    }
    private static final List<Channel> channels = new ArrayList<>();
    private static final List<Recommendation> recommendations = new ArrayList<>();
    private static final List<Recommendation> favourites = new ArrayList<>();
    private static final List<PreviewCommonStructure> previewCommonStructures = new ArrayList<>();

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

    public static Single<List<PreviewCommonStructure>> getPreviewCommonStructureSingle(Context context) {
        return Single.create(emitter -> {
            try {
                List<PreviewCommonStructure> s = getPreviewCommonStructure(context);
                emitter.onSuccess(s);
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    public static List<PreviewCommonStructure> getPreviewCommonStructure(Context context) {
        previewCommonStructures.clear();

        for (LauncherBasedData data : getRecommendations(context)) {
            previewCommonStructures.add(new PreviewCommonStructure(data.getType().name(),
                    data.getID(), data.getName(),
                    data.getBaseIcon(),
                    data.getImageUrl(),
                    data.isActive(), data.getAdditionalData()));
        }
        for (LauncherBasedData data : getChannels(context)) {
            previewCommonStructures.add(new PreviewCommonStructure(data.getType().name(),
                    data.getID(), data.getName(),
                    data.getBaseIcon(),
                    data.getImageUrl(),
                    data.isActive(), data.getAdditionalData()));
        }

        for (LauncherBasedData data : InputSourceHelper.getAsInputs(context)) {
            previewCommonStructures.add(new PreviewCommonStructure(data.getType().name(),
                    data.getID(), data.getName(),
                    data.getBaseIcon(),
                    data.getImageUrl(),
                    data.isActive(), data.getAdditionalData()));
        }

        for (LauncherBasedData data : AppsInfoLoader.checkApps(context, false)) {
            previewCommonStructures.add(new PreviewCommonStructure(data.getType().name(),
                    data.getID(), data.getName(),
                    data.getBaseIcon(),
                    data.getImageUrl(),
                    data.isActive(), data.getAdditionalData()));
        }

        return previewCommonStructures;
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

    private static List<LauncherBasedData> readData(Type typeOfT, LauncherBasedData.TYPE type, Context context) {
        SharedPreferences preferences = getLauncherPreference(type, context);

        if (preferences != null) {
            try {
                String value = preferences.getString(Constants.LAUNCHER_PREF_KEY, "");
                return new Gson().fromJson(value, typeOfT);
            } catch (Exception e) {
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


    public static String getApplicationName(PackageManager packageManager, ApplicationInfo applicationInfo) {
        return applicationInfo.loadLabel(packageManager).toString();
    }
}
