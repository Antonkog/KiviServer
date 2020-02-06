package wezom.kiviremoteserver.environment.bridge;

import android.content.Context;
import androidx.annotation.IntRange;

import com.realtek.tv.AQ;
import com.wezom.kiviremoteserver.environment.IAudioSettings;

import wezom.kiviremoteserver.environment.bridge.driver_set.SoundValues;

public class BridgeAudio implements IAudioSettings {

    private AQ audioPreference;

    public BridgeAudio() {
        audioPreference = new AQ();
    }

    public int getSoundType() {
        return audioPreference.getAudioMode();
    }

    public void setSoundType(int progress) {
        audioPreference.setAudioMode(progress);
    }

    public void setBassLevel(Context context, @IntRange(from = 0, to = 100) int progress) {
        audioPreference.setBassLevel(progress);
    }

    @IntRange(from = 0, to = 100)
    public int getBassLevel(Context context) {
        return audioPreference.getBassLevel();
    }

    public void setTrebleLevel(Context context, @IntRange(from = 0, to = 100) int progress) {
        audioPreference.setTrebleLevel(progress);
    }

    @IntRange(from = 0, to = 100)
    public int getTrebleLevel(Context context) {
        return audioPreference.getTrebleLevel();
    }

    public boolean isUserSoundMode() {
        return SoundValues.getByID(getSoundType()).getID() == SoundValues.SOUND_TYPE_USER.getID();
    }
}
