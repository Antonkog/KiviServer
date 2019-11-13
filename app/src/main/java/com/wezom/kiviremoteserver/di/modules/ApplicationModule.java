package com.wezom.kiviremoteserver.di.modules;

import android.app.Application;
import android.content.Context;

import com.wezom.kiviremoteserver.common.AppsInfoLoader;
import com.wezom.kiviremoteserver.common.DeviceUtils;
import com.wezom.kiviremoteserver.common.KiviCache;
import com.wezom.kiviremoteserver.di.qualifiers.ApplicationContext;
import com.wezom.kiviremoteserver.di.scopes.ApplicationScope;
import com.wezom.kiviremoteserver.net.nsd.NsdUtil;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {
    private Application application;

    public ApplicationModule(Application application) {
        this.application = application;
    }

    @Provides
    @ApplicationContext
    Context provideApplicationContext() {
        return application;
    }

    @Provides
    Application provideApplication() {
        return application;
    }

    @Provides
    @ApplicationScope
    static KiviCache provideKiviCache() {
        return new KiviCache();
    }
//
//    @Provides
//    @ApplicationScope
//    static PublishSubject<TvPlayerEvent> provideTvPlayerEventSubject() {
//        return PublishSubject.create();
//    }

    @Provides
    @ApplicationContext
    static NsdUtil provideNSDUtil(Context context) {
        return new NsdUtil(context);
    }

    @Provides
    @Singleton
    @ApplicationContext
    static AppsInfoLoader provideAppsInfoLoader(Context context, KiviCache cache) {
        return new AppsInfoLoader(context, cache);
    }

    @Provides
    @Singleton
    @ApplicationContext
    static DeviceUtils provideDeviceUtils(AppsInfoLoader appsInfoLoader, Context context) {
        return new DeviceUtils(appsInfoLoader, context);
    }

}
