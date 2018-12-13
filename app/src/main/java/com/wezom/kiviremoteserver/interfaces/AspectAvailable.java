package com.wezom.kiviremoteserver.interfaces;

import android.content.Context;

import com.google.gson.Gson;
import com.wezom.kiviremoteserver.App;
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
        PICTUREMODE
    }

    private HashMap<String, int[]> settings;
    private String jsonStructure;


    // versionName "1.2.11"
    //        versionCode 11
    //todo: parse all enums, extract emum attribute from enum value  or some othe implementation
    //https://www.stubbornjava.com/posts/java-enum-lookup-by-name-or-field-without-throwing-exceptions



    public String getJson(Context context) {
        /*
    	Mstar	Reatek
PictureMode	1,2,3,5,7	        0,1,2,3,4,5,6,7,9
Ratio	0,1,2,3	                1,5,9,10
TemperatureValues	1,2,3,4,5	1,2,3,4,5


     */
       if(this.jsonStructure!=null)return this.jsonStructure;
       else {
           HashMap<String, int[]> currentSettings = new HashMap<>();

           if (App.isTVRealtek()) {
               int[] picture = {0, 1, 2, 3, 4, 5, 6, 7, 9};
               int[] ratio = {1, 5, 9, 10};
               int[] temperatureValues = {1, 2, 3, 4, 5};
               currentSettings.put(VALUE_TYPE.RATIO.name(), ratio);
               currentSettings.put(VALUE_TYPE.TEMPERATUREVALUES.name(), picture);
               currentSettings.put(VALUE_TYPE.PICTUREMODE.name(), temperatureValues);
           } else {
               int[] picture = {1, 2, 3, 5, 7};
               int[] ratio = {0, 1, 2, 3};
               int[] temperatureValues = {1, 2, 3, 4, 5};
               currentSettings.put("RATIO", ratio);
               currentSettings.put("TEMPERATUREVALUES", picture);
               currentSettings.put("PICTUREMODE", temperatureValues);
           }
           this.settings = currentSettings;

           InputSourceHelper helper = new InputSourceHelper();
           List<InputSourceHelper.INPUT_PORT> sources = helper.getPortsList(context);
           int[] ports = new int[sources.size()];

           for (int i = 0; i < sources.size(); i++) {
               ports[i] = sources.get(i).getId();
           }
           settings.put("INPUT_PORT", ports);

           Gson gson = new Gson();
           this.jsonStructure = gson.toJson(AspectAvailable.this);
       }
        return jsonStructure;
    }
}