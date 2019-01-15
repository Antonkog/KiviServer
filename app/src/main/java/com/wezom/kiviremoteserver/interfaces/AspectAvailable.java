package com.wezom.kiviremoteserver.interfaces;

import android.content.Context;

import com.wezom.kiviremoteserver.App;
import com.wezom.kiviremoteserver.environment.EnvironmentInputsHelper;
import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper;

import java.util.HashMap;
import java.util.List;

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


    // versionName "1.2.11"
    //        versionCode 11
    //todo: parse all enums, extract emum attribute from enum value  or some othe implementation
    //https://www.stubbornjava.com/posts/java-enum-lookup-by-name-or-field-without-throwing-exceptions


    public void setValues(Context context, InputSourceHelper inputSourceHelper, EnvironmentInputsHelper inputsHelper) {
        /*

       !!! THIS VALUES IS DEFINED BY MANUFACTURE AND CAN CHANGE !!!
       !!! FOR NOW THERE IS NO WAY TO GET THEM INTERNALLY !!!

    	Mstar	Reatek
PictureMode	1,2,3,5,7	        0,1,2,3,4,5,6,7,9
Ratio	0,1,2,3	                1,5,9,10
TemperatureValues	1,2,3,4,5	1,2,3,4,5


     */


        if (settings != null && !settings.isEmpty()) {
            return;
        } else {
            HashMap<String, int[]> currentSettings = new HashMap<>();

            if (App.isTVRealtek()) {
                int[] picture = {0, 1, 2, 3, 4, 5, 6, 7, 9};
                int[] ratio = {1, 5, 9, 10};
                int[] temperatureValues = {1, 2, 3, 4, 5};
//                int[] hdrValues = {0, 1, 2, 3, 4}; //seems like hdr works only with hdm3 on Realtek - need to change logic

                currentSettings.put(VALUE_TYPE.RATIO.name(), ratio);
                currentSettings.put(VALUE_TYPE.TEMPERATUREVALUES.name(), picture);
                currentSettings.put(VALUE_TYPE.PICTUREMODE.name(), temperatureValues);
//                currentSettings.put(VALUE_TYPE.HDR.name(), hdrValues); //seems like hdr works only with hdm3  - need to change logic
            } else {
                int[] picture = {1, 2, 3, 5, 7};
                int[] ratio = {0, 1, 2, 3};
                int[] temperatureValues = {1, 2, 3, 4, 5};
                int[] hdrValues = {0, 1, 2, 3, 4};

                currentSettings.put(VALUE_TYPE.RATIO.name(), ratio);
                currentSettings.put(VALUE_TYPE.TEMPERATUREVALUES.name(), picture);
                currentSettings.put(VALUE_TYPE.PICTUREMODE.name(), temperatureValues);
                currentSettings.put(VALUE_TYPE.HDR.name(), hdrValues);
            }
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