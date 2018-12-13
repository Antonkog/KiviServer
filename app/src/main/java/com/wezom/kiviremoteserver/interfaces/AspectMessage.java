package com.wezom.kiviremoteserver.interfaces;

import com.wezom.kiviremoteserver.environment.EnvironmentPictureSettings;

import java.util.HashMap;

public class AspectMessage {

    public HashMap<String, Integer> settings;

    public AspectMessage(EnvironmentPictureSettings pictureSettings) {
        if(settings == null) settings = new HashMap<>();
        settings.clear();
        settings.put(ASPECT_VALUE.PICTUREMODE.name(), pictureSettings.getPictureMode());
        settings.put(ASPECT_VALUE.BACKLIGHT.name(), pictureSettings.getBacklight());
        settings.put(ASPECT_VALUE.BRIGHTNESS.name(), pictureSettings.getBrightness());
        settings.put(ASPECT_VALUE.SATURATION.name(), pictureSettings.getSaturation());
        settings.put(ASPECT_VALUE.SHARPNESS.name(), pictureSettings.getSharpness);
        settings.put(ASPECT_VALUE.CONTRAST.name(), pictureSettings.getContrast());
        settings.put(ASPECT_VALUE.HDR.name(), pictureSettings.getHDR());
        settings.put(ASPECT_VALUE.VIDEOARCTYPE.name(), pictureSettings.getVideoArcType());
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
        VIDEOARCTYPE
    }

    @Override
    public String toString() {
        return "AspectMessage{" +
                "settings=" + settings.toString() +
                '}';
    }
}