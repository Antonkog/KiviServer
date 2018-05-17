package com.wezom.kiviremoteserver.di.components;



import com.wezom.kiviremoteserver.di.modules.FragmentModule;
import com.wezom.kiviremoteserver.di.scopes.FragmentScope;
import com.wezom.kiviremoteserver.ui.fragments.HomeFragment;

import dagger.Subcomponent;

@FragmentScope
@Subcomponent(modules = FragmentModule.class)
public interface FragmentComponent {


    void inject(HomeFragment homeFragment);
}
