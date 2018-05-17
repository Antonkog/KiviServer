package com.wezom.kiviremoteserver;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;

import com.crashlytics.android.Crashlytics;
import com.wezom.kiviremoteserver.di.components.ApplicationComponent;
import com.wezom.kiviremoteserver.di.components.DaggerApplicationComponent;
import com.wezom.kiviremoteserver.di.modules.ApplicationModule;
import com.wezom.kiviremoteserver.service.CursorService;
import com.wezom.kiviremoteserver.service.KiviRemoteService;

import java.util.UUID;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

import static com.wezom.kiviremoteserver.common.Constants.APPLICATION_UID;

/**
 * Created by andre on 19.05.2017.
 */

public class App extends Application {
    private static ApplicationComponent appComponent;
    SharedPreferences prefs;

    public static ApplicationComponent getApplicationComponent() {
        return appComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String uid = prefs.getString(APPLICATION_UID, null);

        if (uid == null) {
            prefs.edit().putString(APPLICATION_UID, UUID.randomUUID().toString().substring(3, 7)).apply();
        }

        appComponent = DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        switch (BuildConfig.BUILD_TYPE) {
            case "crash":
            case "release":
                Fabric.with(this, new Crashlytics());
            case "debug":
                Timber.plant(new Timber.DebugTree());
        }

        startService(new Intent(this, KiviRemoteService.class));
        startService(new Intent(this, CursorService.class));
    }
}
