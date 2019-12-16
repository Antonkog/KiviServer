package com.wezom.kiviremoteserver.service.aspect.items;

import com.wezom.kiviremoteserver.R;

import wezom.kiviremoteserver.environment.bridge.driver_set.SoundValues;

public class InputsCardItem extends IFLMCard {

    public InputsCardItem() {

        image = R.drawable.ic_hdmi_icon_copy;
        mainText = R.string.inputs;
        secondText = R.string.hdmi;
        values = null;
    }

    @Override
    public void onClick() {

    }


}
