package wezom.kiviremoteserver.environment.bridge.driver_set;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.wezom.kiviremoteserver.R;
import com.wezom.kiviremoteserver.interfaces.DriverValue;
import com.wezom.kiviremoteserver.service.aspect.values.AvailableValues;
import com.wezom.kiviremoteserver.service.aspect.values.IFLMItems;
import com.wezom.kiviremoteserver.service.aspect.values.TextTypedValues;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public enum TemperatureValues implements TextTypedValues, AvailableValues, IFLMItems {

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
                COLOR_TEMP_WARMER, COLOR_TEMP_WARM,
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

    public static TemperatureValues getInstance() {
        return COLOR_TEMP_NATURE;
    }

    @Override
    public int[] getIds() {
        List<TemperatureValues> modes = Arrays.asList(getSet());
        int[] result = new int[modes.size()];
        for (int i = 0; i < modes.size(); i++) {
            result[i] = modes.get(i).getID();
        }
        return result;
    }

    @Override
    public List<DriverValue> getAsDriverList(Context context) {
        List<TemperatureValues> modes = Arrays.asList(getSet());
        LinkedList<DriverValue> linkedList = new LinkedList<>();
        for (int i = 0; i < modes.size(); i++) {
            TemperatureValues temp = modes.get(i);
            linkedList.add(new DriverValue(TemperatureValues.class.getSimpleName(),
                    context.getResources().getString(temp.getStringResourceID()),
                    temp.getID() + ""
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

}
// COLOR_TEMP_COOL(0, R.string.cool),
//    COLOR_TEMP_NATURE(1, R.string.nature),
//    COLOR_TEMP_WARM(2, R.string.warm);