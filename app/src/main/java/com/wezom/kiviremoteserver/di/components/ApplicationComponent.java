package com.wezom.kiviremoteserver.di.components;


import com.wezom.kiviremoteserver.common.AppsInfoLoader;
import com.wezom.kiviremoteserver.di.modules.ActivityModule;
import com.wezom.kiviremoteserver.di.modules.ApplicationModule;
import com.wezom.kiviremoteserver.di.scopes.ApplicationScope;
import com.wezom.kiviremoteserver.mvp.presenter.HomeFragmentPresenter;
import com.wezom.kiviremoteserver.receiver.AppsChangeReceiver;
import com.wezom.kiviremoteserver.receiver.WifiStateChangesReceiver;
import com.wezom.kiviremoteserver.service.AidlPlayerService;
import com.wezom.kiviremoteserver.service.CursorService;
import com.wezom.kiviremoteserver.service.ExecutorServiceIME;
import com.wezom.kiviremoteserver.service.RemoteReceiverService;
import com.wezom.kiviremoteserver.service.RemoteSenderService;

import dagger.Component;

@ApplicationScope
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {
    ActivityComponent providesActivityComponent(ActivityModule activityModule);

    //Fragments
    void inject(HomeFragmentPresenter homeFragmentPresenter);
    //Service
    void inject(RemoteSenderService remoteSenderService);

    void inject(RemoteReceiverService remoteReceiverService);

    void inject(ExecutorServiceIME executorServiceIME);

    void inject(WifiStateChangesReceiver wifiStateChangesReceiver);

    void inject(CursorService cursorService);

    void inject(AppsInfoLoader appsInfoLoader);

    void inject(AidlPlayerService aidlPlayerService);

    void inject(AppsChangeReceiver appsChangeReceiver);

}
