package com.wezom.kiviremoteserver.service.aspect.values;

import androidx.annotation.StringRes;

public class TimerValues implements IFLMItems {
    @StringRes
    int string;
    int minutes;

    public TimerValues(@StringRes int string, int minutes) {
        this.string = string;
        this.minutes = minutes;
    }


    @Override
    public int getStringRes() {
        return string;
    }

    public int getMinutes() { return minutes; }

    @Override
    public int getId() {
        return 0;
    }
}
