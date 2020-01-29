package com.wezom.kiviremoteserver.environment;

import android.content.Context;

import wezom.kiviremoteserver.environment.bridge.BridgeAudio;

public class EnviorenmentAudioSettings {

    BridgeAudio bridgeAudio;

    public EnviorenmentAudioSettings() {
        //
        bridgeAudio = new BridgeAudio();
    }

    public int getSoundType() {
        return bridgeAudio.getSoundType();
    }

    public void setSoundType(int progress) {
        bridgeAudio.setSoundType(progress);
    }

    public void setBassLevel(Context context, int progress) {
        bridgeAudio.setBassLevel(context, progress);
    }

    public int getBassLevel(Context context) {
        return bridgeAudio.getBassLevel(context);
    }

    public void setTrebleLevel(Context context, int progress) {
        bridgeAudio.setTrebleLevel(context, progress);
    }

    public int getTrebleLevel(Context context) {
        return bridgeAudio.getTrebleLevel(context);
    }

    public boolean isUserSoundMode(){
        return  bridgeAudio.isUserSoundMode();
    }
}
