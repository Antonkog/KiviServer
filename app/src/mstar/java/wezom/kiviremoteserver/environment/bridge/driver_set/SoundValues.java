package wezom.kiviremoteserver.environment.bridge.driver_set;


import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.wezom.kiviremoteserver.R;
import com.wezom.kiviremoteserver.service.aspect.AvailableValues;
import com.wezom.kiviremoteserver.service.aspect.TextTypedValues;

import java.util.Arrays;
import java.util.List;


public enum SoundValues implements TextTypedValues, AvailableValues {

    SOUND_TYPE_STANDARD(1, R.string.sound_standard),
    SOUND_TYPE_VIDEO(2, R.string.sound_film),
    SOUND_TYPE_MUSIC(3, R.string.sound_music),
    SOUND_TYPE_USER(4, R.string.sound_user);

    // public static final int SOUND_MODE_STANDARD = 0;
//    public static final int SOUND_MODE_Movie = 1;
//	public static final int SOUND_MODE_MUSIC = 2;
//	public static final int SOUND_MODE_USER = 3;
    @StringRes
    int stringRes;
    int id;

    @Nullable
    public static SoundValues getByID(int id) {
        for (SoundValues item : values()) {
            if (id == item.id)
                return item;
        }
        return null;
    }

    SoundValues(int id, int stringRes) {
        this.id = id;
        this.stringRes = stringRes;
    }

    public static SoundValues[] getSet() {
        return new SoundValues[]{SOUND_TYPE_STANDARD,
                SOUND_TYPE_VIDEO,SOUND_TYPE_MUSIC,
                SOUND_TYPE_USER};
    }

    @Override
    public int getStringResourceID() {
        return stringRes;
    }

    @Override
    public int getID() {
        return id;
    }

    public static SoundValues getInstance() {
        return SOUND_TYPE_STANDARD;
    }
    @Override
    public int[] getIds() {
        List<SoundValues> modes = Arrays.asList(getSet());
        int[] result = new int[modes.size()];
        for (int i = 0; i < modes.size(); i++) {
            result[i]= modes.get(i).getID();
        }
        return result;
    }
}