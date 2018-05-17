package com.wezom.kiviremoteserver.bus;


public class IMETextEvent {
    private final String text;

    public IMETextEvent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
