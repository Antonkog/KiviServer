package com.wezom.kiviremoteserver.environment;

import android.content.Context;

public interface IAudioSettings {

    int getSoundType();

    void setSoundType(int progress);

    void setTrebleLevel(Context context, int progress);

    void setBassLevel(Context context, int progress);

    int getBassLevel(Context context);

    int getTrebleLevel(Context context);
}
