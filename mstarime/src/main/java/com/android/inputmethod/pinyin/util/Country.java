package com.android.inputmethod.pinyin.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public enum Country {
    UKRAINE(true, "ua"),
    RUSSIA(false, "ru"),
    UNKNOWN(false, "??");
    private String countryCode;
    private boolean isGooglePlayService;
    private static Country currentCountry;
    private static final String TAG = "Country";

    Country(boolean isPlayServiceSupport, String countryCode) {
        this.countryCode = countryCode;
        isGooglePlayService = isPlayServiceSupport;
    }

    public static Country getCountry() {
        if (currentCountry == null) {
            String countryKey = "??";
            switch (Manufacture.getManufacture()) {
                case MSTAR:
                    try {
                        SQLiteDatabase db = SQLiteDatabase.openDatabase("/system/model/model.db",
                                null, SQLiteDatabase.OPEN_READONLY);
                        Cursor cursor = db.rawQuery("select * from  build_info where device_model=?", new String[]{android.os.Build.MODEL});

                        if (cursor != null && cursor.moveToFirst()) {
                            int id = cursor.getColumnIndex("country");
                            countryKey = cursor.getString(id);
                            cursor.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "exception while getting country from mtc database : " + e.getMessage());
                    }
                    break;
                case REALTEK:
                case REALTEK9:
                    countryKey = PropertyHelper.getProperty("ro.product.country");
                    break;
            }

            currentCountry = mapCountryValue(countryKey);

        }
        return currentCountry;
    }

    private static Country mapCountryValue(String value) {
        switch (value.toLowerCase()) {
            case "ukraine":
            case "ukr":
            case "uk":
            case "ua":
                return UKRAINE;
            case "russia":
            case "rus":
            case "ru":
                return RUSSIA;
            default: {
                Log.e(TAG, " wrong country " + value + ", setting default - UA : ");
                return UNKNOWN;
            }
        }
    }


    public String getCode() {
        return countryCode;
    }

    public boolean isGooglePlayService() {
        return isGooglePlayService;
    }
}
