package com.wezom.kiviremoteserver.di.components;


import com.wezom.kiviremoteserver.ui.activity.HomeActivity;
import com.wezom.kiviremoteserver.di.modules.ActivityModule;
import com.wezom.kiviremoteserver.di.modules.FragmentModule;
import com.wezom.kiviremoteserver.di.scopes.ActivityScope;

import dagger.Subcomponent;

@ActivityScope
@Subcomponent(modules = ActivityModule.class)
public interface ActivityComponent {
    FragmentComponent providesFragmentComponent(FragmentModule fragmentModule);

    void inject(HomeActivity homeActivity);
}
