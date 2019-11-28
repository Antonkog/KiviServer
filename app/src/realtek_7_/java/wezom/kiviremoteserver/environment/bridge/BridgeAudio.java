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

    //min max values for audioPreference
    private int maxValue = 20;  // max value from lib
    private int minValue = -20; // min value from lib
    private int delta = maxValue - minValue;

    public void setBassLevel(@IntRange(from = 0, to = 100) int progress) {
        audioPreference.setBassLevel((progress * delta) / 100 + minValue);
    }

    @IntRange(from = 0, to = 100)
    public int getBassLevel() {
        return (audioPreference.getBassLevel() - minValue) * 100 / delta;
    }

    public void setTrebleLevel(@IntRange(from = 0, to = 100) int progress) {
        audioPreference.setTrebleLevel((progress * delta) / 100 + minValue);
    }

    @IntRange(from = 0, to = 100)
    public int getTrebleLevel() {
        return (audioPreference.getTrebleLevel() - minValue) * 100 / delta;
    }
}
