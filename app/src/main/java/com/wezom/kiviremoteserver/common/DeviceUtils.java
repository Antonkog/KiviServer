package com.wezom.kiviremoteserver.common;

import android.app.UiModeManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.text.format.DateUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wezom.kiviremoteserver.net.server.model.AppVisibility;
import com.wezom.kiviremoteserver.net.server.model.Channel;
import com.wezom.kiviremoteserver.net.server.model.LauncherBasedData;
import com.wezom.kiviremoteserver.net.server.model.PreviewCommonStructure;
import com.wezom.kiviremoteserver.net.server.model.Recommendation;
import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper;

import org.jetbrains.annotations.Nullable;

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
    private static long previewsCollectedTime = 0;

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
                if (!previewCommonStructures.isEmpty() &&
                        previewsCollectedTime != 0 &&
                        ((System.currentTimeMillis() - previewsCollectedTime) < DateUtils.HOUR_IN_MILLIS)) {
                    emitter.onSuccess(previewCommonStructures);
                } else {
                    List<PreviewCommonStructure> s = getPreviewCommonStructure(context);
                    emitter.onSuccess(s);
                }
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    public static List<PreviewCommonStructure> getPreviewCommonStructure(Context context) {
        previewCommonStructures.clear();

        for (LauncherBasedData data : getLauncherData(recommendations, LauncherBasedData.TYPE.RECOMMENDATION, context)) {
            previewCommonStructures.add(new PreviewCommonStructure(data.getType().name(),
                    data.getID(), data.getName(),
                    data.getBaseIcon(),
                    data.getImageUrl(),
                    data.isActive(), data.getAdditionalData()));
        }
        for (LauncherBasedData data : getLauncherData(channels, LauncherBasedData.TYPE.CHANNEL, context)) {
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
        previewsCollectedTime = System.currentTimeMillis();
        return previewCommonStructures;
    }

    public static <T extends LauncherBasedData> List<T> getLauncherData(@Nullable List<T> recs, LauncherBasedData.TYPE type, Context context) {
        if (recs == null) recs = new ArrayList<>();
        recs.clear();
        List<LauncherBasedData> recsList = getLauncherPreference(type, context);
        if (recsList != null)
            for (int i = 0; i < recsList.size(); i++) {
                recs.add((T) recsList.get(i));
            }

        Timber.e(" get LauncherBasedData , size is : " + ((recsList == null) ? " null" : recsList.size()));
        return recs;
    }

    private static List<LauncherBasedData> getLauncherPreference(LauncherBasedData.TYPE type, Context context) {

        SharedPreferences p = null;
        try {
            Context myContext = context.createPackageContext(Constants.LAUNCHER_PACKAGE, Context.MODE_PRIVATE);
            switch (type) {
                case RECOMMENDATION:
                    p = myContext.getSharedPreferences(Constants.PREFERENCE_CATEGORY + Constants.RECOMMENDATION_MANAGER, Context.MODE_PRIVATE);
                    return new Gson().fromJson(p.getString(Constants.LAUNCHER_PREF_KEY, ""), new TypeToken<ArrayList<Recommendation>>() {
                    }.getType());
                case CHANNEL:
                    p = myContext.getSharedPreferences(Constants.PREFERENCE_CATEGORY + Constants.CHANNEL_MANAGER, Context.MODE_PRIVATE);
                    return new Gson().fromJson(p.getString(Constants.LAUNCHER_PREF_KEY, ""), new TypeToken<ArrayList<Channel>>() {
                    }.getType());
                case FAVOURITE:
                    p = myContext.getSharedPreferences(Constants.PREFERENCE_CATEGORY + Constants.FAVORITES_MANAGER, Context.MODE_PRIVATE);
                    return new Gson().fromJson(p.getString(Constants.LAUNCHER_PREF_KEY, ""), new TypeToken<ArrayList<Recommendation>>() {
                    }.getType());
                case APPLICATION:
                    p = myContext.getSharedPreferences(Constants.PREFERENCE_CATEGORY + Constants.APP_MANAGER, Context.MODE_PRIVATE);
                    return new Gson().fromJson(p.getString(Constants.LAUNCHER_PREF_KEY, ""), new TypeToken<ArrayList<AppVisibility>>() {
                    }.getType());
            }
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(" launcher based data error, type: " + type.name() + " " + e);
        } catch (Exception e) {
            Timber.e("Can't get launcher based from gson  " + type.name());
        }
        return null;
    }


    public static String getApplicationName(PackageManager packageManager, ApplicationInfo applicationInfo) {
        return applicationInfo.loadLabel(packageManager).toString();
    }
}
