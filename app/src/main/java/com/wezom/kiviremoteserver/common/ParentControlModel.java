package com.wezom.kiviremoteserver.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Set;

public class ParentControlModel {

    private final static String SP_NAME = "sp_pc";
    private final static String SP_KEY = "pc";

    public String pin;
    public boolean pcEnabled;
    public Set<String> excludeInputs = new HashSet<>();

    public static ParentControlModel load(Context context) {
        try {
            Context myContext = context.createPackageContext(Constants.LAUNCHER_PACKAGE, Context.MODE_PRIVATE);
            SharedPreferences sharedPreferences = myContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
            String strPc = sharedPreferences.getString(SP_KEY, null);
            if (!TextUtils.isEmpty(strPc)) {
                return new Gson().fromJson(strPc, ParentControlModel.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isPinSetup(){
        return !TextUtils.isEmpty(pin);
    }

    public boolean isPcEnabled(){
        return isPinSetup() && pcEnabled;
    }
}
