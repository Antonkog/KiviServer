package com.wezom.kiviremoteserver.di.modules;

import android.support.v4.app.Fragment;

import dagger.Module;

@Module
public class FragmentModule {
    Fragment fragment;
    public FragmentModule(Fragment fragment) {
        this.fragment = fragment;
    }
}
