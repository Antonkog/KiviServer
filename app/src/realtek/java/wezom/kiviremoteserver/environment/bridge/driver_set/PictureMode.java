package wezom.kiviremoteserver.environment.bridge.driver_set;


import android.support.annotation.Nullable;

import com.wezom.kiviremoteserver.R;

import java.util.Arrays;
import java.util.List;


public enum PictureMode {

    PICTURE_MODE_NORMAL(9, R.string.normal),
    PICTURE_MODE_SOFT(2, R.string.soft),
    PICTURE_MODE_USER(0, R.string.user),
    PICTURE_MODE_AUTO(7, R.string.auto),
    PICTURE_MODE_MOVIE(4, R.string.movie),
    PICTURE_MODE_SPORT(5, R.string.sport),
    PICTURE_MODE_GAME(6, R.string.game),
    PICTURE_MODE_VIVID(1, R.string.vivid);
    // PICTURE_MODE_ECONOMY(10, R.string.economy);


    int id;
    int string;

    PictureMode(int id, int string) {
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
    public static PictureMode getByID(int id) {
        for (PictureMode port : values()) {
            if (port.id == id) {
                return port;
            }
        }
        return null;
    }

    //List<PictureMode> modes = ;
    public static List<PictureMode> getModes() {
        return Arrays.asList(PICTURE_MODE_NORMAL,
                PICTURE_MODE_SOFT,
                PICTURE_MODE_USER,
                PICTURE_MODE_AUTO,
                PICTURE_MODE_MOVIE,
                PICTURE_MODE_SPORT,
                PICTURE_MODE_GAME,
                PICTURE_MODE_VIVID);
    }
}

//Arrays.asList(new PictureMode[]{PICTURE_MODE_NORMAL,
//            PICTURE_MODE_SOFT,
//            PICTURE_MODE_USER,
//            PICTURE_MODE_AUTO,
//            PICTURE_MODE_VIVID})
//PICTURE_MODE_NORMAL(1, R.string.normal),
//    PICTURE_MODE_SOFT(2, R.string.soft),
//    PICTURE_MODE_USER(3, R.string.user),
//    PICTURE_MODE_AUTO(5, R.string.economy),
//    PICTURE_MODE_VIVID(7, R.string.vivid);
