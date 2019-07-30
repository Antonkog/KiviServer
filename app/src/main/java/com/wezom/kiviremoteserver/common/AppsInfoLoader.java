package com.wezom.kiviremoteserver.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import com.wezom.kiviremoteserver.common.extensions.ViewExtensionsKt;
import com.wezom.kiviremoteserver.net.server.model.ServerApplicationInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import io.reactivex.Single;
import timber.log.Timber;

/**
 * Created by antonio on 11/3/17.
 */

public class AppsInfoLoader {
    private static final List<String> whiteListSystemApps = new ArrayList<>();

    public static Single<List<ServerApplicationInfo>> getAppsList(Context context) {
        return Single.create(emitter ->
                emitter.onSuccess(checkApps(context, true))
        );
    }

    // private final int sortMode = 0;
//
//    /**
//     * @param sortMode 0 = sort by name
//     *                 2 = sort by size
//     *                 3 = sort by Installation Date
//     *                 4 = sort by Last Update
//     * @return  List<ServerApplicationInfo> list of apps installed on device - system permission should be provided.
//     */
//
//    public void setSortMode(int sortMode) {
//        this.sortMode = sortMode;
//    }

    /**
     * @return List<AppInfo> list of apps installed on device - you should provide system permission to use this method;
     */

    public static List<ServerApplicationInfo> checkApps(Context ctx, Boolean withIconOldApi) {
        getWhiteList(ctx);

        int sortMode = 0;

        ArrayList<ServerApplicationInfo> appList = new ArrayList();

        final PackageManager packageManager = ctx.getPackageManager();
        List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);

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
                Collections.sort(packages, new Comparator<PackageInfo>() {
                    @Override
                    public int compare(PackageInfo p1, PackageInfo p2) {
                        Long size1 = new File(p1.applicationInfo.sourceDir).length();
                        Long size2 = new File(p2.applicationInfo.sourceDir).length();
                        return size2.compareTo(size1);
                    }
                });
                break;
            case 3:
                // Comparator by Installation Date (default)
                Collections.sort(packages, new Comparator<PackageInfo>() {
                    @Override
                    public int compare(PackageInfo p1, PackageInfo p2) {
                        return Long.toString(p2.firstInstallTime).compareTo(Long.toString(p1.firstInstallTime));
                    }
                });
                break;
            case 4:
                // Comparator by Last Update
                Collections.sort(packages, new Comparator<PackageInfo>() {
                    @Override
                    public int compare(PackageInfo p1, PackageInfo p2) {
                        return Long.toString(p2.lastUpdateTime).compareTo(Long.toString(p1.lastUpdateTime));
                    }
                });
                break;
        }

        // Installed & System Apps
        for (PackageInfo packageInfo : packages) {
            try {
                if (!(packageManager.getApplicationLabel(packageInfo.applicationInfo).equals("") || packageInfo.packageName.equals(""))) {
                    if (packageManager.getLaunchIntentForPackage(packageInfo.packageName) != null && isNotExcluded(packageInfo)) {
                        Drawable drawable = packageManager.getApplicationBanner(packageInfo.applicationInfo);
                        if (drawable == null)
                            drawable = packageManager.getApplicationIcon(packageInfo.applicationInfo);
                        if (drawable == null)
                            drawable = packageManager.getApplicationLogo(packageInfo.applicationInfo);
                        if (drawable != null) {
                            int width = drawable.getIntrinsicWidth();
                            int height = drawable.getIntrinsicHeight();

                            byte[] icon = ViewExtensionsKt.getIconBytes(ctx, width == 0 ? Constants.APP_ICON_W : width, height == 0 ? Constants.APP_ICON_H : height, drawable);

                            ServerApplicationInfo tempApp = new ServerApplicationInfo()
                                    .setApplicationName(packageManager.getApplicationLabel(packageInfo.applicationInfo).toString())
                                    .setApplicationPackage(packageInfo.packageName)
                                    .setBaseIcon(Base64.encodeToString(icon, Base64.DEFAULT));

                            if (withIconOldApi) tempApp.setApplicationIcon(icon);
                            appList.add(tempApp);
                        }
                    }
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return appList;
    }

    private static boolean isNotExcluded(PackageInfo info) {
        if (info.packageName.equals("com.ua.mytrinity.tvplayer.kivitv"))
            return true;

        if (info.packageName.equals("com.wezom.kiviremoteserver"))
            return false;

        if (info.packageName.equals("com.kivi.launcher_v2"))
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

}
