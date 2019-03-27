package com.wezom.kiviremoteserver.environment;

import android.content.Context;
import android.preference.PreferenceManager;

import com.wezom.kiviremoteserver.common.Constants;
import com.wezom.kiviremoteserver.service.aspect.HDRValues;

import wezom.kiviremoteserver.environment.bridge.BridgePicture;
import wezom.kiviremoteserver.environment.bridge.driver_set.SoundValues;


public class EnvironmentPictureSettings {
    private int redColor;
    private int greenColor;
    private int blueColor;
    private int pictureMode;
    private int brightness;
    private int saturation;
    private int backlight;
    private int temperature;
    private int HDR;
    private int green;
    private int BLue;
    private int red;
    private int sharpness;
    private int contrast;
    private int videoArcType;

    //
    BridgePicture bridgePicture;

    public EnvironmentPictureSettings() {
        //
        bridgePicture = new BridgePicture();
    }

    public void setPictureMode(int pictureMode) {
        //
        this.pictureMode = pictureMode;
        bridgePicture.setPictureMode(pictureMode);
    }

    public int getPictureMode() {
        //
        return bridgePicture.getPictureMode();
    }

    public void initColors() {
        bridgePicture.initColors();

    }

    public int getRedColor() {
        return bridgePicture.getRedColor();
    }

    public int getGreenColor() {
        return bridgePicture.getGreenColor();
    }

    public int getBlueColor() {
        return bridgePicture.getBlueColor();
    }

    public void initSettings(Context context) {
        bridgePicture.initSettings(context);


    }

    public int getBrightness() {
        return bridgePicture.getBrightness();
    }

    public int getSharpness() {
        return bridgePicture.getSharpness();
    }
    public int getSaturation() {
        return bridgePicture.getSaturation();
    }

    public int getBacklight() {
        return bridgePicture.getBacklight();
    }

    public int getTemperature() {
        //TvPictureManager.getInstance().getColorTempratureIdx()
        return bridgePicture.getTemperature();
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
        bridgePicture.setTemperature(temperature);
        //TvPictureManager.getInstance().setColorTempratureIdx(progress)
    }

    public int getHDR() {
        //TvPictureManager.getInstance().getHdrAttributes(TvPictureManager.HDR_OPEN_ATTRIBUTES,
        //                TvPictureManager.VIDEO_MAIN_WINDOW).level
        return bridgePicture.getHDR();
    }

    public void setHDR(int HDR) {
        this.HDR = HDR;
        bridgePicture.setHDR(HDR);
        //TvPictureManager.getInstance().setHdrAttributes(TvPictureManager.HDR_OPEN_ATTRIBUTES,
        //                    TvPictureManager.VIDEO_MAIN_WINDOW, progress);
    }

    public void setGreen(int green) {
        this.green = green;
        bridgePicture.setGreen(green);
        //TvPictureManager.getInstance().setVideoItem(
        //                TvPictureManager.PICTURE_SHARPNESS, progress)
    }

    public void setBLue(int BLue) {
        this.BLue = BLue;
        bridgePicture.setBLue(BLue);
    }

    public void setRed(int red) {
        this.red = red;
        bridgePicture.setRed(red);
        //TvPictureManager.getInstance().setVideoItem(
        //                TvPictureManager.PICTURE_SHARPNESS, progress)
    }

    public void setSharpness(int sharpness) {
        this.sharpness = sharpness;
        bridgePicture.setSharpness(sharpness);
    }

    public void setSaturation(int saturation) {
        this.saturation = saturation;
        bridgePicture.setSaturation(saturation);
    }

    public void setContrast(int contrast) {
        this.contrast = contrast;
        bridgePicture.setContrast(contrast);
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
        bridgePicture.setBrightness(brightness);
    }

    public void setBacklight(int backlight, Context context) {
        this.backlight = backlight;
        bridgePicture.setBacklight(backlight);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Constants.LAST_BACKLIGHT, backlight).commit();
    }

    public int getVideoArcType() {
        return bridgePicture.getVideoArcType();
    }

    public void setVideoArcType(int videoArcType) {
        //
        this.videoArcType = videoArcType;
        bridgePicture.setVideoArcType(videoArcType);
    }

    public boolean isSafe() {
        return bridgePicture.isSafe();
    }

    public HDRValues[] getHDRSet() {
        return HDRValues.getInstance().getHDRSet();
    }

    public int getContrast() {
        return bridgePicture.getContrast();
    }

    public int getSoundType() {
        return bridgePicture.getSoundType();
    }

    public void setSoundType(int progress) {
        bridgePicture.setSoundType(progress);
    }

    public void setBassLevel(Context context, int progress) {
        if(isUserSoundMode())    PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Constants.LAST_BASS, progress).commit();
        bridgePicture.setBassLevel(progress);
    }

    public int getBassLevel(Context context) {
        if(isUserSoundMode()) return PreferenceManager.getDefaultSharedPreferences(context).getInt(Constants.LAST_BASS, Constants.FIFTY);
        return bridgePicture.getBassLevel();
    }

    public void setTrebleLevel(Context context, int progress) {
        if(isUserSoundMode())  PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Constants.LAST_TREBLE, progress).commit();
        bridgePicture.setTrebleLevel(progress);
    }

    public int getTrebleLevel(Context context) {
        if(isUserSoundMode())  return PreferenceManager.getDefaultSharedPreferences(context).getInt(Constants.LAST_TREBLE, Constants.FIFTY);
        return bridgePicture.getTrebleLevel();
    }

    public boolean isUserSoundMode(){
      return  SoundValues.getByID(getSoundType()).getID() == SoundValues.SOUND_TYPE_USER.getID();
    }
}
