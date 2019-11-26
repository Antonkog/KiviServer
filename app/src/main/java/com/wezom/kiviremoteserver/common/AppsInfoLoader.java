package com.wezom.kiviremoteserver.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.util.Base64;

import com.wezom.kiviremoteserver.common.extensions.ViewExtensionsKt;
import com.wezom.kiviremoteserver.di.qualifiers.ApplicationContext;
import com.wezom.kiviremoteserver.net.server.model.AppVisibility;
import com.wezom.kiviremoteserver.net.server.model.LauncherBasedData;
import com.wezom.kiviremoteserver.net.server.model.PreviewContent;
import com.wezom.kiviremoteserver.net.server.model.ServerApplicationInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.reactivex.Single;
import timber.log.Timber;

/**
 * Created by antonio on 11/3/17.
 */

public class AppsInfoLoader implements SyncValue {
    private static final List<String> visibleApps = new ArrayList<>();
    private Context context;
    private ArrayList<ServerApplicationInfo> appList = new ArrayList();
    private long syncFrequency = 20 * DateUtils.MINUTE_IN_MILLIS;
    private static long appsCollectedTime = 0;

    private KiviCache cache;

    @Inject
    public AppsInfoLoader(@ApplicationContext Context context, KiviCache cache) {
        this.context = context;
        this.cache = cache;
    }

    @Override
    public void init(Context context) {
        Timber.e("AppsInfoLoader init ");
        int size =  checkApps(context, true).size();
        Timber.e("checkApps  size = " + size);
    }

    @Override
    public long getSyncFrequency() {
        return syncFrequency;
    }

    public static boolean isUserApp(ApplicationInfo info) {
        int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        return (info.flags & mask) == 0;
    }

    public List<PreviewContent> getImgByIds(List<String> ids) {
        List<PreviewContent> previewContents = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            if (cache.get(ids.get(i)) != null) {
                previewContents.add(new PreviewContent(LauncherBasedData.TYPE.APPLICATION.name(), ids.get(i), cache.get(ids.get(i))));
            }
        }
        return previewContents;
    }

    public Single<List<PreviewContent>> getPreviewsById(List<String> ids) {
        return Single.create(emitter ->
                emitter.onSuccess(getImgByIds(ids))
        );
    }


    public Single<List<ServerApplicationInfo>> getAppsList() {
        return Single.create(emitter ->
                emitter.onSuccess(checkApps(context, true))
        );
    }


    /**
     * @param ctx
     * @param withIconOldApi
     * @return List<AppInfo> list of apps installed on device - you should provide system permission to use this method;
     */
    public  List<ServerApplicationInfo> checkApps(Context ctx, Boolean withIconOldApi) {
        if (!appList.isEmpty() &&
                appsCollectedTime != 0 &&
                ((System.currentTimeMillis() - appsCollectedTime) < syncFrequency)) {
            return appList;
        } else {
            appList.clear();
            getWhiteList(ctx);
            final PackageManager packageManager = ctx.getPackageManager();
            List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);
            sort(packageManager, packages, 0);

            // Installed & System Apps
            for (PackageInfo packageInfo : packages) {
                try {
                    if (!(packageManager.getApplicationLabel(packageInfo.applicationInfo).equals("") || packageInfo.packageName.equals(""))) {
                        if (packageManager.getLaunchIntentForPackage(packageInfo.packageName) != null && isNotExcluded(packageInfo)) {
                            ServerApplicationInfo tempApp = new ServerApplicationInfo()
                                    .setApplicationName(packageManager.getApplicationLabel(packageInfo.applicationInfo).toString())
                                    .setApplicationPackage(packageInfo.packageName);
                            if (withIconOldApi) {
                                addDrawable(ctx, packageManager, packageInfo, tempApp);
                            }
                            appList.add(tempApp);
                        }
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
            }
            return appList;
        }
    }

    private void addDrawable(Context ctx, PackageManager packageManager, PackageInfo packageInfo, ServerApplicationInfo tempApp) {
        Drawable drawable = packageManager.getApplicationBanner(packageInfo.applicationInfo);
        if (drawable == null)
            drawable = packageManager.getApplicationIcon(packageInfo.applicationInfo);
        if (drawable == null)
            drawable = packageManager.getApplicationLogo(packageInfo.applicationInfo);
        if (drawable != null) {
            byte[] icon = ViewExtensionsKt.getIconBytes(ctx, ViewExtensionsKt.dpToPx(ctx, Constants.APP_ICON_W), ViewExtensionsKt.dpToPx(ctx, Constants.APP_ICON_H), drawable);
            Timber.e("adding drawable to cache ");
            cache.put(packageInfo.packageName, Base64.encodeToString(icon, Base64.DEFAULT));
            tempApp.setApplicationIcon(icon);
        }
    }

    /**
     * @param packageManager
     * @param packages       package infos
     * @param sortMode       0 = sort by name
     *                       2 = sort by size
     *                       3 = sort by Installation Date
     *                       4 = sort by Last Update
     * @return List<ServerApplicationInfo> list of apps installed on device - system permission should be provided.
     */
    private void sort(PackageManager packageManager, List<PackageInfo> packages, int sortMode) {
        switch (sortMode) {
            default:
                // Comparator by Name (default)
                Collections.sort(packages, new Comparator<PackageInfo>() {
                    @Override
                    public int compare(PackageInfo p1, PackageInfo p2) {
                        return packageManager.getApplicationLabel(p1.applicationInfo).toString().toLowerCase().compareTo(packageManager.getApplicationLabel(p2.applicationInfo).toString().toLowerCase());
                    }
                });
                break;
            case 2:
                // Comparator by Size
                Collections.sort(packages, (p1, p2) -> {
                    Long size1 = new File(p1.applicationInfo.sourceDir).length();
                    Long size2 = new File(p2.applicationInfo.sourceDir).length();
                    return size2.compareTo(size1);
                });
                break;
            case 3:
                // Comparator by Installation Date (default)
                Collections.sort(packages, (p1, p2) -> Long.toString(p2.firstInstallTime).compareTo(Long.toString(p1.firstInstallTime)));
                break;
            case 4:
                // Comparator by Last Update
                Collections.sort(packages, (p1, p2) -> Long.toString(p2.lastUpdateTime).compareTo(Long.toString(p1.lastUpdateTime)));
                break;
        }
    }

    private static boolean isNotExcluded(PackageInfo info) {
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

    private static boolean isSystemApp(PackageInfo packageInfo) {
        return ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM);
    }

    private static boolean isInWhiteList(String packageName) {
        return visibleApps.contains(packageName);
    }

    private static List<String> getWhiteList(Context context) {
        Context launcher2Context = null;
        try {
            launcher2Context = context.createPackageContext(Constants.LAUNCHER_PACKAGE, Context.MODE_PRIVATE);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(" getting white list, no " + Constants.LAUNCHER_PACKAGE  + " context, trying to get list from old launcher " + e.getMessage());
            e.printStackTrace();
        }
        if (launcher2Context != null) {
            List<AppVisibility> appVisibilities = DeviceUtils.getLauncherData(null, LauncherBasedData.TYPE.APPLICATION, launcher2Context);
            if (appVisibilities != null && appVisibilities.size() > 0)
                visibleApps.clear();
            for (AppVisibility app : appVisibilities) {
                if (app.isActive()) {
                    visibleApps.add(app.getPackageName());
                }
            }
        } else {
            Set<String> apps;// that is for old devices with first launcher should be remobe
            try {
                Context myContext = context.createPackageContext("com.kivi.launcher",
                        Context.MODE_PRIVATE);

                SharedPreferences testPrefs = myContext.getSharedPreferences
                        ("kivi.launcher", Context.MODE_PRIVATE);
                apps = testPrefs.getStringSet("white_list", null);
                if (apps != null) {
                    visibleApps.clear();
                    visibleApps.addAll(apps);
                } else {
                    Timber.e("getWhiteList empty");
                }
            } catch (PackageManager.NameNotFoundException e) {
                Timber.e(" getting white list, no com.kivi.launcher context "  + e.getMessage());
                Timber.e(e, e.getMessage());
            }
        }
        return visibleApps;
    }

}
