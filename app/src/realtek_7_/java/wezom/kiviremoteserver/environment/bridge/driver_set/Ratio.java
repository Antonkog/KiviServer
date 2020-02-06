package wezom.kiviremoteserver.environment.bridge.driver_set;

import android.content.Context;

import androidx.annotation.Nullable;

import com.wezom.kiviremoteserver.R;
import com.wezom.kiviremoteserver.interfaces.DriverValue;
import com.wezom.kiviremoteserver.service.aspect.values.AvailableValues;
import com.wezom.kiviremoteserver.service.aspect.values.IFLMItems;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public enum Ratio implements AvailableValues, IFLMItems {

    VIDEO_ARC_DEFAULT(1, R.string.default_r),
    VIDEO_ARC_16x9(9, R.string.r_16x9),
    VIDEO_ARC_4x3(5, R.string.r_4x3),
    VIDEO_ARC_AUTO(10, R.string.auto);
//    VIDEO_ARC_DEFAULT(1, R.string.default_r),
//    VIDEO_ARC_16x9(10, R.string.r_16x9),
//    VIDEO_ARC_4x3(5, R.string.r_4x3),
//    VIDEO_ARC_AUTO(8, R.string.auto);

    int id;
    int string;

    Ratio(int id, int string) {
        this.id = id;
        this.string = string;
    }

    public int getId() {
        return id;
    }

    public int getString() {
        return string;
    }

    @Nullable
    public static Ratio getByID(int id) {
        for (Ratio port : values()) {
            if (port.id == id) {
                return port;
            }
        }
        return null;
    }

    public static Ratio getInstance() {
        return VIDEO_ARC_DEFAULT;
    }

    @Override
    public int[] getIds() {
        List<Ratio> modes = getRatios();
        int[] result = new int[modes.size()];
        for (int i = 0; i < modes.size(); i++) {
            result[i] = modes.get(i).getId();
        }
        return result;
    }

    @Override
    public List<DriverValue> getAsDriverList(Context context) {
        List<Ratio> modes = getRatios();
        LinkedList<DriverValue> linkedList = new LinkedList<>();
        for (int i = 0; i < modes.size(); i++) {
            Ratio temp = modes.get(i);
            linkedList.add(new DriverValue(Ratio.class.getSimpleName(),
                    context.getResources().getString(temp.getString()),
                    temp.getId() + ""
                    , temp.getId(),
                    false));
        }
        return linkedList;
    }

    @Override
    public int getStringRes() {
        return string;
    }


    public static  List<Ratio> getRatios() {
        return Arrays.asList(
                Ratio.VIDEO_ARC_DEFAULT,
                Ratio.VIDEO_ARC_16x9,
                Ratio.VIDEO_ARC_4x3,
                Ratio.VIDEO_ARC_AUTO);
    }
}