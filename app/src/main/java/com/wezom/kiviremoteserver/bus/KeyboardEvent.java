package com.wezom.kiviremoteserver.bus;

public class KeyboardEvent {
    private boolean isMuteEvent;

    public KeyboardEvent(boolean isMuteEvent) {
        this.isMuteEvent = isMuteEvent;
    }

    public boolean isMuteEvent() {
        return isMuteEvent;
    }
}
