package com.wezom.kiviremoteserver.bus;


public class SendVolumeEvent {
    private int volumeLevel;

    public SendVolumeEvent(int volumeLevel) {
        this.volumeLevel = volumeLevel;
    }

    public int getVolumeLevel() {
        return volumeLevel;
    }
}
