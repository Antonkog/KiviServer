package com.wezom.kiviremoteserver.di.modules;

import android.app.Application;
import android.content.Context;

import com.wezom.kiviremoteserver.di.qualifiers.ApplicationContext;
import com.wezom.kiviremoteserver.net.nsd.NsdRegistrator;

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
    static NsdRegistrator provideCategoryRepository(Context context) {
        return new NsdRegistrator(context);
    }

}
