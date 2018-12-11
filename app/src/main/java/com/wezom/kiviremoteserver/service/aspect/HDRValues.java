package com.wezom.kiviremoteserver.service.aspect;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.wezom.kiviremoteserver.R;


public enum HDRValues implements TextTypedValues {
    HDR_OPEN_LEVEL_OFF(0, R.string.off),
    HDR_OPEN_LEVEL_AUTO(1, R.string.auto),
    HDR_OPEN_LEVEL_LOW(2, R.string.low),
    HDR_OPEN_LEVEL_MIDDLE(3, R.string.middle),
    HDR_OPEN_LEVEL_HIGH(4, R.string.high);

    @StringRes
    int stringRes;
    int id;

    HDRValues(int id, int stringRes) {
        this.id = id;
        this.stringRes = stringRes;
    }
    @Nullable
    public static HDRValues getByID(int id){
        for(HDRValues item :values()){
            if(id == item.id)
                return item;
        }
        return null;
    }
    @Override
    public int getStringResourceID() {
        return stringRes;
    }

    @Override
    public int getID() {
        return id;
    }
}
