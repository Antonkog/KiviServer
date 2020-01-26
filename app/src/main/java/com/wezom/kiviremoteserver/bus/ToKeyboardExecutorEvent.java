package com.wezom.kiviremoteserver.bus;

public class ToKeyboardExecutorEvent {


    private int keyCode;
    private int type;
    private String text;
    public final static int
            NOT_SPECIFIED = 0,
            CLICK = 1,
            TEXT = 2,
            COMMAND_NORMAL = 3;

    public ToKeyboardExecutorEvent(int commandType, int keyCode, String text) {
        this.type = commandType;
        this.keyCode = keyCode;
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public int getKeyCode() {
        return keyCode;
    }
}
