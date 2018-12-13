package com.wezom.kiviremoteserver.environment;

import android.content.Context;

import com.wezom.kiviremoteserver.service.aspect.HDRValues;

import wezom.kiviremoteserver.environment.bridge.BridgePicture;


public class EnvironmentPictureSettings {
    private int redColor;
    private int greenColor;
    private int blueColor;
    private int pictureMode;
    private int brightness;
    public int getSharpness;
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

    public void setBacklight(int backlight) {
        this.backlight = backlight;
        bridgePicture.setBacklight(backlight);
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

        return bridgePicture.getHDRSet();
    }

    public int getContrast() {
        return bridgePicture.getContrast();
    }
}