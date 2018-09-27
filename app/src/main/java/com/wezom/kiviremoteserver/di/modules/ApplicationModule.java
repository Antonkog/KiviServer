package com.wezom.kiviremoteserver.di.modules;

import android.app.Application;
import android.content.Context;

import com.wezom.kiviremoteserver.di.qualifiers.ApplicationContext;
import com.wezom.kiviremoteserver.net.nsd.NsdUtil;

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
    @ApplicationContext
    static NsdUtil provideNSDUtil(Context context) {
        return new NsdUtil(context);
    }

}
