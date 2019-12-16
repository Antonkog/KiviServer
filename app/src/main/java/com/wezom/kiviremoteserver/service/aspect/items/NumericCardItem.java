package com.wezom.kiviremoteserver.service.aspect.items;

import com.wezom.kiviremoteserver.R;

import wezom.kiviremoteserver.environment.bridge.driver_set.Ratio;

public class NumericCardItem extends IFLMCard {

    public NumericCardItem() {

        image = R.drawable.ic_numeric_icon;
        mainText = R.string.keyboard;
        secondText = R.string.keyboard_desk;
        values = null;
    }

    @Override
    public void onClick() {

    }


}
