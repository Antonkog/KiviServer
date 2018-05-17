package com.wezom.kiviremoteserver.di.components;



import com.wezom.kiviremoteserver.di.modules.ActivityModule;
import com.wezom.kiviremoteserver.di.modules.ApplicationModule;
import com.wezom.kiviremoteserver.di.scopes.ApplicationScope;
import com.wezom.kiviremoteserver.mvp.presenter.HomeFragmentPresenter;
import com.wezom.kiviremoteserver.receiver.WifiStateChangesReceiver;
import com.wezom.kiviremoteserver.service.CursorService;
import com.wezom.kiviremoteserver.service.KiviRemoteService;

import dagger.Component;

@ApplicationScope
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {
    ActivityComponent providesActivityComponent(ActivityModule activityModule);

    //Fragments
    void inject(HomeFragmentPresenter homeFragmentPresenter);

    //Service
    void inject(KiviRemoteService kiviRemoteService);

    void inject(WifiStateChangesReceiver wifiStateChangesReceiver);

    void inject(CursorService cursorService);
}
