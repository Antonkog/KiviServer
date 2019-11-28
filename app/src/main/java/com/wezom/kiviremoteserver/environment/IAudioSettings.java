package com.wezom.kiviremoteserver.environment;

public interface IAudioSettings {

    int getSoundType();

    void setSoundType(int progress);

    void setTrebleLevel(int progress);

    void setBassLevel(int progress);

    int getBassLevel();

    int getTrebleLevel();
}
