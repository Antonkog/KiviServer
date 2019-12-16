package com.wezom.kiviremoteserver.service.aspect.items;

import com.wezom.kiviremoteserver.R;

import java.util.Arrays;
import java.util.List;

public class SettingCardItem extends IFLMCard {
    List<Integer> shutDownTimers = Arrays.asList(0, 15, 30, 60, 90, 120, 180);

    public SettingCardItem() {
        image = R.drawable.ic_settings_icon;
        mainText = R.string.settings;
        secondText = R.string.settings_desk;
        values = null;
    }

    @Override
    public void onClick() {

    }


}
