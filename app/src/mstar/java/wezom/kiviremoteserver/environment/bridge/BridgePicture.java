package wezom.kiviremoteserver.environment.bridge;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.WindowManager;

import com.android.inputmethod.pinyin.util.PropertyHelper;
import com.mstar.android.tv.TvCommonManager;
import com.mstar.android.tv.TvPictureManager;
import com.mstar.android.tvapi.common.vo.ColorTemperatureExData;

import static android.os.Build.MODEL;

public class BridgePicture {
    public static int LAYER_TYPE = WindowManager.LayoutParams.TYPE_TOAST;
    private final boolean isUHD;
    TvPictureManager pictureManager;
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

        pictureManager = TvPictureManager.getInstance();
    }

    public void setPictureMode(int pictureMode) {
        TvPictureManager.getInstance().setPictureMode(pictureMode);
        TvPictureManager.getInstance().setPictureMode(pictureMode);
    }

    public int getPictureMode() {
        return TvPictureManager.getInstance().getPictureMode();
    }

    public void initColors() {
        ColorTemperatureExData colorTemperatureExData = TvPictureManager
                .getInstance().getColorTempratureEx();
        color_r = colorTemperatureExData.redGain;
        color_g = colorTemperatureExData.greenGain;
        color_b = colorTemperatureExData.blueGain;
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
        int mPictureMode = pictureManager.getPictureMode();
        int inputSrcType = TvCommonManager.getInstance().getCurrentTvInputSource();
        Cursor cursor = context.getApplicationContext()
                .getContentResolver()
                .query(Uri.parse("content://mstar.tv.usersetting/picmode_setting/inputsrc/"
                                + inputSrcType + "/picmode/" + mPictureMode), null,
                        null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            contrast = cursor.getInt(cursor
                    .getColumnIndex("u8Contrast"));
            brightness = cursor.getInt(cursor
                    .getColumnIndex("u8Brightness"));
            sharpness = cursor.getInt(cursor
                    .getColumnIndex("u8Sharpness"));
            saturation = cursor.getInt(cursor
                    .getColumnIndex("u8Saturation"));
            backlight = cursor.getInt(cursor
                    .getColumnIndex("u8Backlight"));
        }
        cursor.close();
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
        return TvPictureManager.getInstance().getBacklight();
    }

    public int getTemperature() {
        return TvPictureManager.getInstance().getColorTempratureIdx();
    }

    public void setTemperature(int temperature) {
        TvPictureManager.getInstance().setColorTempratureIdx(temperature);
    }

    public int getHDR() {
        return TvPictureManager.getInstance().getHdrAttributes(TvPictureManager.HDR_OPEN_ATTRIBUTES,
                TvPictureManager.VIDEO_MAIN_WINDOW).level;
    }

    public void setHDR(int HDR) {
        TvPictureManager.getInstance().setHdrAttributes(TvPictureManager.HDR_OPEN_ATTRIBUTES,
                TvPictureManager.VIDEO_MAIN_WINDOW, HDR);
    }

    public void setGreen(int green) {
        TvPictureManager.getInstance().setVideoItem(
                TvPictureManager.PICTURE_SHARPNESS, green);
    }

    public void setBLue(int BLue) {
        TvPictureManager.getInstance().setVideoItem(
                TvPictureManager.PICTURE_SHARPNESS, BLue);

    }

    public void setRed(int red) {
        TvPictureManager.getInstance().setVideoItem(
                TvPictureManager.PICTURE_SHARPNESS, red);
    }

    public void setSharpness(int sharpness) {
        TvPictureManager.getInstance().setVideoItem(
                TvPictureManager.PICTURE_SHARPNESS, sharpness);
    }

    public void setSaturation(int saturation) {
        TvPictureManager.getInstance().setVideoItem(
                TvPictureManager.PICTURE_SATURATION, saturation);
    }

    public void setContrast(int contrast) {
        TvPictureManager.getInstance().setVideoItem(
                TvPictureManager.PICTURE_CONTRAST, contrast);
    }

    public void setBrightness(int brightness) {
        TvPictureManager.getInstance().setVideoItem(
                TvPictureManager.PICTURE_BRIGHTNESS, brightness);
    }

    public void setBacklight(int backlight) {
        TvPictureManager.getInstance().setBacklight(backlight);
    }

    public int getVideoArcType() {
        return TvPictureManager.getInstance().getVideoArcType();
    }

    public void setVideoArcType(int videoArcType) {
        TvPictureManager.getInstance().setVideoArcType(videoArcType);
    }

    public boolean isSafe() {

        int mInputSource = TvCommonManager.getInstance().getCurrentTvInputSource();
        return !isUHD || (isPlay() || (mInputSource != TvCommonManager.INPUT_SOURCE_STORAGE));

    }
    private boolean isPlay() {
        String string = PropertyHelper.getSystemProp("Storage_Video_Status", "Finalize");
        // Log.e("isSafe", "string " + string);
        return "inited".equals(string);
    }

    public int getContrast() {
        return contrast;
    }

}
