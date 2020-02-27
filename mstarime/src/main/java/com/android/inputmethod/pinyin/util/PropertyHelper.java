package com.android.inputmethod.pinyin.util;

import java.lang.reflect.Method;

public class PropertyHelper {
    private static final String propertyClassName = "android.os.SystemProperties";
    private static final String propertyGet = "get";

    public static String getSystemProp(String prop, String value) {
        String string = "";
        try {

            Class properties = Class.forName("android.os.SystemProperties");
            Method setProp = properties.getMethod("get", new Class[]{String.class, String.class});
            string = (String) setProp.invoke(properties, new Object[]{prop, value});

        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }


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
