package com.android.inputmethod.pinyin.util;

public enum Manufacture {
    REALTEK(),
    MSTAR();

    private static Manufacture currentManufacture;

    Manufacture() {

    }

    static Manufacture getManufacture() {
        if (currentManufacture == null) {
            currentManufacture = isTVRealtek() ? REALTEK : MSTAR;
        }
        return currentManufacture;
    }

    public static boolean isTVRealtek() {
        return "realtek".equalsIgnoreCase(PropertyHelper.getProperty("ro.product.manufacturer"));
    }

}
