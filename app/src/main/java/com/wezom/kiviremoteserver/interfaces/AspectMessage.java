package com.wezom.kiviremoteserver.interfaces;

import com.wezom.kiviremoteserver.App;
import com.wezom.kiviremoteserver.BuildConfig;
import com.wezom.kiviremoteserver.common.Constants;
import com.wezom.kiviremoteserver.environment.EnvironmentInputsHelper;
import com.wezom.kiviremoteserver.environment.EnvironmentPictureSettings;

import java.util.HashMap;

public class AspectMessage {

    public HashMap<String, Integer> settings;

    public AspectMessage(EnvironmentPictureSettings pictureSettings, EnvironmentInputsHelper environmentInputsHelper) {
        if (settings == null) settings = new HashMap<>();
        settings.clear();
        settings.put(ASPECT_VALUE.PICTUREMODE.name(), pictureSettings.getPictureMode());
        settings.put(ASPECT_VALUE.BACKLIGHT.name(), pictureSettings.getBacklight());
        settings.put(ASPECT_VALUE.BRIGHTNESS.name(), pictureSettings.getBrightness());
        settings.put(ASPECT_VALUE.SATURATION.name(), pictureSettings.getSaturation());
        settings.put(ASPECT_VALUE.SHARPNESS.name(), pictureSettings.getSharpness());
        settings.put(ASPECT_VALUE.CONTRAST.name(), pictureSettings.getContrast());
        settings.put(ASPECT_VALUE.HDR.name(), pictureSettings.getHDR());
        settings.put(ASPECT_VALUE.VIDEOARCTYPE.name(), pictureSettings.getVideoArcType());
        settings.put(ASPECT_VALUE.INPUT_PORT.name(), environmentInputsHelper.getCurrentTvInputSource());
        settings.put(ASPECT_VALUE.SERVER_VERSION_CODE.name(), BuildConfig.VERSION_CODE);
        settings.put(ASPECT_VALUE.MANUFACTURE.name(), App.isTVRealtek() ? Constants.SERV_REALTEK : Constants.SERV_MSTAR);
    }

    public enum ASPECT_VALUE {
        PICTUREMODE,
        BRIGHTNESS,
        SHARPNESS,
        SATURATION,
        BACKLIGHT,
        TEMPERATURE,
        HDR,
        GREEN,
        BLUE,
        RED,
        CONTRAST,
        VIDEOARCTYPE,
        INPUT_PORT,
        SERVER_VERSION_CODE,
        MANUFACTURE
    }

    @Override
    public String toString() {
        return "AspectMessage{" +
                "settings=" + settings.toString() +
                '}';
    }
}