package com.android.inputmethod.pinyin.util;

import java.lang.reflect.Method;

public class PropertyHelper {
    private static final String propertyClassName = "android.os.SystemProperties";
    private static final String propertyGet = "get";


    public static String getProperty(String value) {
        String model = "";
        try {
            Class<?> c = Class.forName(propertyClassName);
            Method get = c.getMethod(propertyGet, String.class);
            model = (String) get.invoke(c, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }
}
