package wezom.kiviremoteserver.environment.bridge;

import android.content.Context;
import android.support.annotation.IntRange;
import android.util.Log;
import android.view.WindowManager;

import com.realtek.tv.AQ;
import com.realtek.tv.ColorTempInfo;
import com.realtek.tv.PQ;
import com.realtek.tv.VSC;
import com.wezom.kiviremoteserver.service.aspect.values.HDRValues;

public class BridgePicture {
    private PQ picturePreference;
    private AQ audioPreference;
    private VSC widthMode;

    public BridgePicture() {
        widthMode = new VSC();
        audioPreference = new AQ();
        picturePreference = new PQ();//picturePreference.getColorTempBias()
        // audioPreference.setlevel()
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

    public void setBlue(int blue) {
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

    public int getSoundType() {
        return audioPreference.getAudioMode();
    }

    public void setSoundType(int progress) {
        audioPreference.setAudioMode(progress);
    }

    //min max values for audioPreference
    private int maxValue = 20;  // max value from lib
    private int minValue = -20; // min value from lib
    private int delta = maxValue - minValue;

    public void setBassLevel(@IntRange(from = 0, to = 100) int progress) {
        audioPreference.setBassLevel((progress * delta) / 100 + minValue);
    }

    @IntRange(from = 0, to = 100)
    public int getBassLevel() {
        return (audioPreference.getBassLevel() - minValue) * 100 / delta;
    }

    public void setTrebleLevel(@IntRange(from = 0, to = 100) int progress) {
        audioPreference.setTrebleLevel((progress * delta) / 100 + minValue);
    }

    @IntRange(from = 0, to = 100)
    public int getTrebleLevel() {
        return (audioPreference.getTrebleLevel() - minValue) * 100 / delta;
    }

    @IntRange(from = 0, to = 100)
    public int getBalanceLevel() { return (audioPreference.getBalanceLevel() - minValue) * 100 / delta; }
    public void setBalanceLevel(@IntRange(from = 0, to = 100) int progress) {
        audioPreference.setBalanceLevel((progress * delta) / 100 + minValue);
    }

    public void setDolbyLevel(int progress) {
        audioPreference.setDolbyAudioLevel(progress);
    }

    public int getDolbyLevel() {
        return audioPreference.getDolbyAudioLevel();
    }

}
