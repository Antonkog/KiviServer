package com.wezom.kiviremoteserver.service.aspect.items;

import android.support.annotation.StringRes;

import com.wezom.kiviremoteserver.R;

import java.util.Arrays;
import java.util.List;

public class TimerValues implements IFLMItems {
    @StringRes
    int string;

    TimerValues(@StringRes int string) {
        this.string = string;
    }


    @Override
    public int getStringRes() {
        return string;
    }

    @Override
    public int getId() {
        return 0;
    }
}
