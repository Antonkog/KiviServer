package com.wezom.kiviremoteserver.di.modules;

import androidx.fragment.app.Fragment;

import dagger.Module;

@Module
public class FragmentModule {
    Fragment fragment;
    public FragmentModule(Fragment fragment) {
        this.fragment = fragment;
    }
}
