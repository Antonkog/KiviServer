package com.wezom.kiviremoteserver.interfaces;

import android.content.Context;

import com.wezom.kiviremoteserver.environment.EnvironmentInputsHelper;
import com.wezom.kiviremoteserver.service.aspect.HDRValues;
import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper;

import java.util.HashMap;
import java.util.List;

import wezom.kiviremoteserver.environment.bridge.driver_set.PictureMode;
import wezom.kiviremoteserver.environment.bridge.driver_set.Ratio;
import wezom.kiviremoteserver.environment.bridge.driver_set.TemperatureValues;

public class AspectAvailable {
    private static class InstanceHolder {
        private static final AspectAvailable INSTANCE = new AspectAvailable();
    }
    public static AspectAvailable getInstance() {
        return InstanceHolder.INSTANCE;
    }


    public enum VALUE_TYPE {
        INPUT_PORT,
        RATIO,
        TEMPERATUREVALUES,
        HDR,
        PICTUREMODE
    }

    public HashMap<String, int[]> settings;

    public void setValues(Context context, InputSourceHelper inputSourceHelper, EnvironmentInputsHelper inputsHelper) {
        if (settings != null && !settings.isEmpty()) {
            return;
        } else {
            HashMap<String, int[]> currentSettings = new HashMap<>();
            currentSettings.put(VALUE_TYPE.RATIO.name(), Ratio.getInstance().getIds());
            currentSettings.put(VALUE_TYPE.TEMPERATUREVALUES.name(), PictureMode.getInstance().getIds());
            currentSettings.put(VALUE_TYPE.PICTUREMODE.name(), TemperatureValues.getInstance().getIds());
            currentSettings.put(VALUE_TYPE.HDR.name(), HDRValues.getInstance().getIds());
            this.settings = currentSettings;

            List<InputSourceHelper.INPUT_PORT> sources = inputSourceHelper.getPortsList(context);
            int[] ports = new int[sources.size() + 1]; //current is last
            for (int i = 0; i < sources.size(); i++) {
                ports[i] = sources.get(i).getId();
            }
            ports[ports.length -1] = inputsHelper.getCurrentTvInputSource(); //current port
            settings.put(VALUE_TYPE.INPUT_PORT.name(), ports);
            context = null;
        }
    }
}