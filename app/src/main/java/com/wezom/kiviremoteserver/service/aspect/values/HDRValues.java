package com.wezom.kiviremoteserver.service.aspect.values;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.wezom.kiviremoteserver.R;
import com.wezom.kiviremoteserver.interfaces.DriverValue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public enum HDRValues implements TextTypedValues, AvailableValues, IFLMItems {
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

    public static HDRValues getInstance() {
        return HDR_OPEN_LEVEL_AUTO;
    }

    @Override
    public int[] getIds() {
        List<HDRValues> modes = Arrays.asList(getHDRSet());
        int[] result = new int[modes.size()];
        for (int i = 0; i < modes.size(); i++) {
            result[i]= modes.get(i).getID();
        }
        return result;
    }

    @Override
    public List<DriverValue> getAsDriverList(Context context) {
        List<HDRValues> modes = Arrays.asList(getHDRSet());
        LinkedList<DriverValue> linkedList = new LinkedList<>();
        for (int i = 0; i < modes.size(); i++) {
            HDRValues temp = modes.get(i);
            linkedList.add(new DriverValue(
                    HDRValues.class.getName(),
                    context.getResources().getString(temp.getStringResourceID()),
                    temp.getID()+""
                    , temp.getID(),
                    false));
        }
        return linkedList;
    }

    @Override
    public int getStringRes() {
        return stringRes;
    }

    @Override
    public int getId() {
        return id;
    }

    public HDRValues[] getHDRSet() {
        return new HDRValues[]{HDR_OPEN_LEVEL_AUTO,
                HDR_OPEN_LEVEL_LOW,
                HDR_OPEN_LEVEL_MIDDLE,
                HDR_OPEN_LEVEL_HIGH,
                HDR_OPEN_LEVEL_OFF};
    }
}
