package wezom.kiviremoteserver.environment.bridge;

import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.annotation.IntRange;

import com.crashlytics.android.Crashlytics;
import com.realtek.tv.AQ;
import com.wezom.kiviremoteserver.common.Constants;
import com.wezom.kiviremoteserver.environment.IAudioSettings;

import wezom.kiviremoteserver.environment.bridge.driver_set.SoundValues;

public class BridgeAudio implements IAudioSettings {

    private AQ audioPreference;

    public BridgeAudio() {

        audioPreference = new AQ();
    }

    public int getSoundType() {
        if (audioPreference != null)
            return audioPreference.getAudioMode();
        else {
            Crashlytics.logException(new Throwable("audioPreference is null " + Build.MODEL + Build.ID));
            return 0;
        }
    }

    public void setSoundType(int progress) {
        audioPreference.setAudioMode(progress);
    }

    //min max values for audioPreference
    private int maxValue = 20;  // max value from lib
    private int minValue = -20; // min value from lib
    private int delta = maxValue - minValue;

    public void setBassLevel(Context context, @IntRange(from = 0, to = 100) int progress) {
        if (isUserSoundMode())
            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Constants.LAST_BASS, progress).commit();
        audioPreference.setBassLevel((progress * delta) / 100 + minValue);
    }

    @IntRange(from = 0, to = 100)
    public int getBassLevel(Context context) {
        if (isUserSoundMode())
            return PreferenceManager.getDefaultSharedPreferences(context).getInt(Constants.LAST_BASS, Constants.FIFTY);
        return (audioPreference.getBassLevel() - minValue) * 100 / delta;
    }

    public void setTrebleLevel(Context context, @IntRange(from = 0, to = 100) int progress) {
        if (isUserSoundMode())
            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Constants.LAST_TREBLE, progress).commit();
        audioPreference.setTrebleLevel((progress * delta) / 100 + minValue);
    }

    @IntRange(from = 0, to = 100)
    public int getTrebleLevel(Context context) {
        if (isUserSoundMode())
            return PreferenceManager.getDefaultSharedPreferences(context).getInt(Constants.LAST_TREBLE, Constants.FIFTY);
        return (audioPreference.getTrebleLevel() - minValue) * 100 / delta;
    }

    public boolean isUserSoundMode() {
        return SoundValues.getByID(getSoundType()).getID() == SoundValues.SOUND_TYPE_USER.getID();
    }
}
