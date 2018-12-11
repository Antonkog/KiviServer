package wezom.kiviremoteserver.environment.bridge.driver_set;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.wezom.kiviremoteserver.R;
import com.wezom.kiviremoteserver.service.aspect.TextTypedValues;

public enum TemperatureValues implements TextTypedValues {

    COLOR_TEMP_NATURE(1, R.string.nature),
    COLOR_TEMP_WARMER(2, R.string.warmer),
    COLOR_TEMP_WARM(3, R.string.warm),
    COLOR_TEMP_COOL(4, R.string.cool),
    COLOR_TEMP_COOLER(5, R.string.cooler);


    @StringRes
    int stringRes;
    int id;

    @Nullable
    public static TemperatureValues getByID(int id) {
        for (TemperatureValues item : values()) {
            if (id == item.id)
                return item;
        }
        return null;
    }

    TemperatureValues(int id, int stringRes) {
        this.id = id;
        this.stringRes = stringRes;
    }

    public static TemperatureValues[] getSet() {
        return new TemperatureValues[]{COLOR_TEMP_NATURE,
                COLOR_TEMP_WARMER,COLOR_TEMP_WARM,
                COLOR_TEMP_COOL, COLOR_TEMP_COOLER};
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
// COLOR_TEMP_COOL(0, R.string.cool),
//    COLOR_TEMP_NATURE(1, R.string.nature),
//    COLOR_TEMP_WARM(2, R.string.warm);