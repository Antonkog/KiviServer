package com.wezom.kiviremoteserver.bus;

public class VolumeEvent {
    private boolean isMuteEvent;

    public VolumeEvent(boolean isMuteEvent) {
        this.isMuteEvent = isMuteEvent;
    }

    public boolean isMuteEvent() {
        return isMuteEvent;
    }
}
