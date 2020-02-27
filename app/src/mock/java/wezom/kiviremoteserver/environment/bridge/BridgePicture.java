package wezom.kiviremoteserver.environment.bridge;

import android.content.Context;
import android.view.WindowManager;

import static android.os.Build.MODEL;

public class BridgePicture {
    public static int LAYER_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
    private final boolean isUHD;
    private int color_r;
    private int color_g;
    private int color_b;
    private int contrast;
    private int brightness;
    private int sharpness;
    private int saturation;
    private int backlight;
    private int temperature;

    public BridgePicture() {
        isUHD = MODEL != null && (MODEL.indexOf("U")>0 && MODEL.indexOf("U")<5);

    }

    public void setPictureMode(int pictureMode) {

    }

    public int getPictureMode() {
        return 0;
    }

    public void initColors() {

    }

    public int getRedColor() {
        return color_r;
    }

    public int getGreenColor() {
        return color_g;
    }

    public int getBlueColor() {
        return color_b;
    }

    public void initSettings(Context context) {

    }

    public int getBrightness() {
        return brightness;
    }

    public int getSaturation() {
        return saturation;
    }

    public int getSharpness() {
        return sharpness;
    }

    public int getBacklight() {
        return 0;
    }

    public int getTemperature() {
        return 0;
    }

    public void setTemperature(int temperature) {

    }

    public int getHDR() {
        return 0;
    }

    public void setHDR(int HDR) {

    }

    public void setGreen(int green) {

    }

    public void setBLue(int BLue) {

    }

    public void setRed(int red) {
       }

    public void setSharpness(int sharpness) {
    }

    public void setSaturation(int saturation) {
    }

    public void setContrast(int contrast) {
    }

    public void setBrightness(int brightness) {
    }

    public void setBacklight(int backlight) {
     }

    public int getVideoArcType() {
        return 0;
    }

    public void setVideoArcType(int videoArcType) {
    }

    public boolean isSafe() {
        return true;
    }



    public int getContrast() {
        return contrast;
    }

}
