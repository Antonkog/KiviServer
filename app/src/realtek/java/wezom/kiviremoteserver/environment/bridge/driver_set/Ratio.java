package wezom.kiviremoteserver.environment.bridge.driver_set;

import android.support.annotation.Nullable;

import com.wezom.kiviremoteserver.R;


public enum Ratio {

    VIDEO_ARC_DEFAULT(1, R.string.default_r),
    VIDEO_ARC_16x9(9, R.string.r_16x9),
    VIDEO_ARC_4x3(5, R.string.r_4x3),
    VIDEO_ARC_AUTO(10, R.string.auto);

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
}

//VIDEO_ARC_DEFAULT(0, R.string.default_r),
//    VIDEO_ARC_16x9(1, R.string.r_16x9),
//    VIDEO_ARC_4x3(2, R.string.r_4x3),
//    VIDEO_ARC_AUTO(3, R.string.auto);
