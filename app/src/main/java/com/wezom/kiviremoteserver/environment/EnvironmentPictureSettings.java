package com.wezom.kiviremoteserver.environment;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.wezom.kiviremoteserver.R;
import com.wezom.kiviremoteserver.common.Constants;
import com.wezom.kiviremoteserver.service.aspect.HDRValues;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import wezom.kiviremoteserver.environment.bridge.BridgePicture;
import wezom.kiviremoteserver.environment.bridge.driver_set.SoundValues;


public class EnvironmentPictureSettings {
    private int redColor;
    private int greenColor;
    private int blueColor;
    private int pictureMode;
    private int brightness = 50;
    private int saturation = 50;
    private int backlight;
    private int temperature = -1;
    private int HDR;
    private int green;
    private int blue;
    private int red;
    private int sharpness;
    private int contrast = 50;
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
        updateValues();
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

    public void setBlue(int blue) {
        this.blue = blue;
        bridgePicture.setBlue(blue);
    }

    public void setRed(int red) {
        this.red = red;
        bridgePicture.setRed(red);
        //TvPictureManager.getInstance().setVideoItem(
        //                TvPictureManager.PICTURE_SHARPNESS, progress)
    }

    public void updateValues() {
//        if(true)
//            return;
        if(temperature<1){
            temperature = getTemperature();
            contrast = getContrast();
            brightness = getBrightness();
            saturation = getSaturation();
        }
        float difContr = ((float) (contrast * 0.6 + 20) - 50f) / 50f;
        float lBrightness = ((float) (brightness * 0.6 + 20) - 50f) / 50f;
        float lSaturation = ((float) saturation) / 50f;
        if (lSaturation > 1) {
            lSaturation = (float) Math.sqrt(lSaturation);
        }
//        COLOR_TEMP_NATURE(1, R.string.nature),
//                COLOR_TEMP_WARMER(2, R.string.warmer),
//                COLOR_TEMP_WARM(3, R.string.warm),
//                COLOR_TEMP_COOL(4, R.string.cool),
//                COLOR_TEMP_COOLER(5, R.string.cooler);

        float tempR = 0;
        float tempG = 0;
        float tempB = 0;
        switch (temperature) {
            case 2:
                tempR = 2;
                tempG = -1;
                tempB = -1;
                break;
            case 3:
                tempR = 4;
                tempG = -1;
                tempB = -2;
                break;
            case 4:
                tempR = -1;
                tempG = -1;
                tempB = 2;
                break;
            case 5:
                tempR = -2;
                tempG = -1;
                tempB = 4;
                break;
            default:
                break;
        }
        tempR = tempR / 100f;
        tempG = tempG / 100f;
        tempB = tempB / 100f;
        float lContrast = 1 + difContr / (3 - 2 * lSaturation);
        float brightnessShift = -0.5f * (difContr);

        lBrightness += brightnessShift;
        final float invSat = 1 - lSaturation;
        final float R = 0.213f * invSat;
        final float G = 0.715f * invSat;
        final float B = 0.072f * invSat;

        float[] matrixVal = new float[]{
                (R + lSaturation) * lContrast, R * lContrast, R * lContrast, 0,
                G * lContrast, (G + lSaturation) * lContrast, G * lContrast, 0,
                B * lContrast, B * lContrast, (B + lSaturation) * lContrast, 0,
                lBrightness + tempR, lBrightness + tempG, lBrightness + tempB, 1};
        setColorTransform(matrixVal);
    }

    private void setColorTransform(float[] m) {
        try {
            Class localClass = Class.forName("android.os.ServiceManager");
            IBinder flinger = null;//android.os.ServiceManager.getService("SurfaceFlinger");
            Method getService = localClass.getMethod("getService", new Class[]{String.class});
            if (getService != null) {
                Object result = getService.invoke(localClass, new Object[]{"SurfaceFlinger"});
                if (result != null) {
                    flinger = (IBinder) result;
                }
            }
            if (flinger != null) {
                final Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                if (m != null) {
                    data.writeInt(1);
                    for (int i = 0; i < 16; i++) {
                        data.writeFloat(m[i]);
                    }
                } else {
                    data.writeInt(0);
                }
                flinger.transact(1015, data, null, 0);
                data.recycle();
            }
        } catch (RemoteException ex) {
            Log.e("RemoteException", "Failed to set color transform", ex);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

//    float saturation = 50;
//    float contrast = 50;
//    float brightness = 50;


    public void setSharpness(int sharpness) {
        this.sharpness = sharpness;
        bridgePicture.setSharpness(sharpness);
    }

    public void setSaturation(int saturation) {
        this.saturation = saturation;
        bridgePicture.setSaturation(saturation);
        updateValues();
    }

    public void setContrast(int contrast) {
        this.contrast = contrast;
        bridgePicture.setContrast(contrast);
        updateValues();
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
        bridgePicture.setBrightness(brightness);
        updateValues();
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
        if (isUserSoundMode())
            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Constants.LAST_BASS, progress).commit();
        bridgePicture.setBassLevel(progress);
    }

    public int getBassLevel(Context context) {
        if (isUserSoundMode())
            return PreferenceManager.getDefaultSharedPreferences(context).getInt(Constants.LAST_BASS, Constants.FIFTY);
        return bridgePicture.getBassLevel();
    }

    public void setTrebleLevel(Context context, int progress) {
        if (isUserSoundMode())
            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Constants.LAST_TREBLE, progress).commit();
        bridgePicture.setTrebleLevel(progress);
    }

    public int getTrebleLevel(Context context) {
        if (isUserSoundMode())
            return PreferenceManager.getDefaultSharedPreferences(context).getInt(Constants.LAST_TREBLE, Constants.FIFTY);
        return bridgePicture.getTrebleLevel();
    }

    public boolean isUserSoundMode() {
        return SoundValues.getByID(getSoundType()).getID() == SoundValues.SOUND_TYPE_USER.getID();
    }
}
