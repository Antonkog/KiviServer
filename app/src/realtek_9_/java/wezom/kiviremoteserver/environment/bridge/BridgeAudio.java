package wezom.kiviremoteserver.environment.bridge;

import android.support.annotation.IntRange;

import com.realtek.tv.AQ;
import com.wezom.kiviremoteserver.environment.IAudioSettings;

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

    public void setBassLevel(@IntRange(from = 0, to = 100) int progress) {
        audioPreference.setBassLevel(progress);
    }

    @IntRange(from = 0, to = 100)
    public int getBassLevel() {
        return audioPreference.getBassLevel();
    }

    public void setTrebleLevel(@IntRange(from = 0, to = 100) int progress) {
        audioPreference.setTrebleLevel(progress);
    }

    @IntRange(from = 0, to = 100)
    public int getTrebleLevel() {
        return audioPreference.getTrebleLevel();
    }
}
