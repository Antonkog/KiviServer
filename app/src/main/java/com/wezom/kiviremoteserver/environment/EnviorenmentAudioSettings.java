package com.wezom.kiviremoteserver.environment;

import android.content.Context;
import android.preference.PreferenceManager;

import com.wezom.kiviremoteserver.common.Constants;

import wezom.kiviremoteserver.environment.bridge.BridgeAudio;
import wezom.kiviremoteserver.environment.bridge.driver_set.SoundValues;

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
        if(isUserSoundMode())    PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Constants.LAST_BASS, progress).commit();
        bridgeAudio.setBassLevel(progress);
    }

    public int getBassLevel(Context context) {
        if(isUserSoundMode()) return PreferenceManager.getDefaultSharedPreferences(context).getInt(Constants.LAST_BASS, Constants.FIFTY);
        return bridgeAudio.getBassLevel();
    }

    public void setTrebleLevel(Context context, int progress) {
        if(isUserSoundMode())  PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(Constants.LAST_TREBLE, progress).commit();
        bridgeAudio.setTrebleLevel(progress);
    }

    public int getTrebleLevel(Context context) {
        if(isUserSoundMode())  return PreferenceManager.getDefaultSharedPreferences(context).getInt(Constants.LAST_TREBLE, Constants.FIFTY);
        return bridgeAudio.getTrebleLevel();
    }

    public boolean isUserSoundMode(){
        return  SoundValues.getByID(getSoundType()).getID() == SoundValues.SOUND_TYPE_USER.getID();
    }
}
