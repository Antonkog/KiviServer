package wezom.kiviremoteserver.environment.bridge;

import android.content.Context;
import android.util.Log;
import android.view.WindowManager;

import com.realtek.tv.ColorTempInfo;
import com.realtek.tv.PQ;
import com.realtek.tv.VSC;
import com.wezom.kiviremoteserver.service.aspect.HDRValues;

public class BridgePicture {
    private PQ picturePreference;
    private VSC widthMode;

    public BridgePicture() {
        widthMode = new VSC();
        picturePreference = new PQ();//picturePreference.getColorTempBias()

    }

    public void setPictureMode(int pictureMode) {
        picturePreference.setPictureMode(pictureMode);


        // picturePreference.setSDR2HDR()
    }

    public int getPictureMode() {
    //    Log.e("pic", "mode " + picturePreference.getPictureMode());
        return picturePreference.getPictureMode();
    }

    public static final int LAYER_TYPE = WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG;
    ColorTempInfo colorTempInfo;

    public void initColors() {
        colorTempInfo = picturePreference.getColorTempPara();
    }

    public int getRedColor() {
        return colorTempInfo.getrGain();
    }

    public int getGreenColor() {
        return colorTempInfo.getgGain();
    }

    public int getBlueColor() {
        return colorTempInfo.getbGain();
    }

    public void initSettings(Context context) {

    }

    public int getBrightness() {
        Log.e("pic", "getBrightness " + picturePreference.getBrightness());
        return picturePreference.getBrightness();
    }

    public int getSharpness() {
        Log.e("pic", "getSharpness " + picturePreference.getSharpness());
        return picturePreference.getSharpness();
    }


    public int getSaturation() {
        Log.e("pic", "getSaturation " + picturePreference.getSaturation());
        return picturePreference.getSaturation();
    }

    public int getBacklight() {
        Log.e("pic", "getBacklight " + picturePreference.getBacklight());
        return picturePreference.getBacklight();
    }

    public int getTemperature() {
        Log.e("pic", "getTemperature " + picturePreference.getColorTemp());
        return picturePreference.getColorTemp();
    }

    public void setTemperature(int temperature) {
        picturePreference.setColorTemp(temperature);
    }

    public int getHDR() {
        Log.e("pic", "getHdr10Enable " + picturePreference.getHdr10Enable());

        return picturePreference.getHdr10Enable() ?
                HDRValues.HDR_OPEN_LEVEL_AUTO.getID() :
                HDRValues.HDR_OPEN_LEVEL_OFF.getID();
    }

    public void setHDR(int HDR) {
        picturePreference.setHdr10Enable(
                HDR != HDRValues.HDR_OPEN_LEVEL_OFF.getID());
    }

    public void setGreen(int green) {
    }

    public void setBLue(int BLue) {
    }

    public void setRed(int red) {
    }

    public void setSharpness(int sharpness) {
        picturePreference.setSharpness(sharpness);
    }

    public void setSaturation(int saturation) {
        picturePreference.setSaturation(saturation);
    }

    public void setContrast(int contrast) {
        picturePreference.setContrast(contrast);
    }

    public void setBrightness(int brightness) {
        picturePreference.setBrightness(brightness);
    }

    public void setBacklight(int backlight) {
        picturePreference.setBacklight(backlight);
    }

    public int getVideoArcType() {
        return widthMode.getWideMode(0);
    }

    public void setVideoArcType(int videoArcType) {
        widthMode.setWideMode(0, videoArcType);
    }

    public HDRValues[] getHDRSet() {
        return new HDRValues[]{HDRValues.HDR_OPEN_LEVEL_AUTO,
                HDRValues.HDR_OPEN_LEVEL_OFF};
    }

    public int getContrast() {
        return picturePreference.getContrast();
    }

    public boolean isSafe() {
        return true;
    }
}
