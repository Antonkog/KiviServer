package com.wezom.kiviremoteserver.di.modules;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.media.AudioManager;

import com.wezom.kiviremoteserver.common.AppsInfoLoader;
import com.wezom.kiviremoteserver.common.DeviceUtils;
import com.wezom.kiviremoteserver.common.KiviCache;
import com.wezom.kiviremoteserver.di.qualifiers.ApplicationContext;
import com.wezom.kiviremoteserver.di.scopes.ApplicationScope;
import com.wezom.kiviremoteserver.net.nsd.NsdUtil;
import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static android.content.Context.AUDIO_SERVICE;

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
    @ApplicationScope
    AudioManager provideAudioManager() {
        return (AudioManager) application.getSystemService(AUDIO_SERVICE);
    }

    @Provides
    Application provideApplication() {
        return application;
    }

    @Provides
    @ApplicationScope
    Instrumentation provideInstrumentation() {
        return new Instrumentation();
    }

    @Provides
    InputSourceHelper provideInputSourceHelper() {
        return new InputSourceHelper();
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
