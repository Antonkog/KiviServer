package com.wezom.kiviremoteserver.di.modules;

import android.support.v7.app.AppCompatActivity;

import dagger.Module;


@Module
public class ActivityModule {
    AppCompatActivity activity;

    public ActivityModule(AppCompatActivity activity) {
        this.activity = activity;
    }
}
