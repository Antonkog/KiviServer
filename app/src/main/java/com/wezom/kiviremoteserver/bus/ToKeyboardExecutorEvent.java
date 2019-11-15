package com.wezom.kiviremoteserver.bus;

public class ToKeyboardExecutorEvent {


    public enum CommandType {
        NOT_SPECIFIED,
        CLICK,
        TEXT,
        COMMAND_NORMAL
    }

    private int keyCode;
    private int type;
    private String text;

    public ToKeyboardExecutorEvent(CommandType commandType, int keyCode, String text) {
        this.type = commandType.ordinal();
        this.keyCode = keyCode;
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public CommandType getType(int num) {
        for(CommandType type : CommandType.values()){
            if(type.ordinal() == num) return type;
        }
        return CommandType.NOT_SPECIFIED;
    }

    public int getKeyCode() {
        return keyCode;
    }
}
