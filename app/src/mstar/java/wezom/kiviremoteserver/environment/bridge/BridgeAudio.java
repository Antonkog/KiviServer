package wezom.kiviremoteserver.environment.bridge;

import android.content.Context;
import android.preference.PreferenceManager;

import com.wezom.kiviremoteserver.common.Constants;
import com.wezom.kiviremoteserver.environment.IAudioSettings;

import wezom.kiviremoteserver.environment.bridge.driver_set.SoundValues;

public class BridgeAudio  implements IAudioSettings {

    public int getSoundType() {
        return 0;
    }

    public void setSoundType(int progress) {

    }

    public void setTrebleLevel(Context context, int progress) {
        if (isUserSoundMode()) PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Constants.LAST_TREBLE, progress).commit();
    }

    public void setBassLevel(Context context, int progress) {
        if (isUserSoundMode()) PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Constants.LAST_BASS, progress).commit();
    }

    public int getBassLevel(Context context) {
        if (isUserSoundMode()) return PreferenceManager.getDefaultSharedPreferences(context).getInt(Constants.LAST_BASS, Constants.FIFTY);
        return 0;
    }

    public int getTrebleLevel(Context context) {
        if (isUserSoundMode()) return PreferenceManager.getDefaultSharedPreferences(context).getInt(Constants.LAST_TREBLE, Constants.FIFTY);
        return 0;
    }

    public boolean isUserSoundMode() {
        return SoundValues.getByID(getSoundType()).getID() == SoundValues.SOUND_TYPE_USER.getID();
    }
}
