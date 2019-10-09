package com.android.inputmethod.pinyin.util;


import android.os.Build;

public enum Manufacture {
    REALTEK9(),
    REALTEK(),
    MSTAR();

    private static Manufacture currentManufacture;

    Manufacture() {

    }

    static Manufacture getManufacture() {
        if (currentManufacture == null) {
            currentManufacture = isTVRealtek() ? REALTEK : MSTAR;
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O) currentManufacture = REALTEK9;
        }
        return currentManufacture;
    }

    public static boolean isTVRealtek() {
        return "realtek".equalsIgnoreCase(PropertyHelper.getProperty("ro.product.manufacturer"));
    }

}
