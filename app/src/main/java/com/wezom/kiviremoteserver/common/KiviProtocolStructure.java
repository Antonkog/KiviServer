package com.wezom.kiviremoteserver.common;

/**
 * Created by andre on 02.06.2017.
 */

public class KiviProtocolStructure {

    public enum ExecActionEnum {
        @Deprecated text,
        TEXT,
        @Deprecated keyevent,
        KEY_EVENT,
        @Deprecated motion,
        MOTION,
        @Deprecated leftClick,
        LEFT_CLICK,
        @Deprecated rightClick,
        RIGHT_CLICK,
        REQUEST_APPS,
        REQUEST_ASPECT,
        LAUNCH_APP,
        REQUEST_VOLUME,
        PING,
        SWITCH_OFF,
        SCROLL,
        OPEN_SETTINGS,
        HOME_DOWN,
        HOME_UP,
        LAUNCH_QUICK_APPS,
        NAME_CHANGE
    }

    public enum ServerEventType {
        KEYBOARD_NOT_SET,
        KEYBOARD_200,
        SHOW_KEYBOARD,
        HIDE_KEYBOARD,
        VOLUME,
        DISCONNECT,
        PONG,
        RECONNECTION_NEED,
        OK200,
        CONN_ESTABLISHED,
        COORDINATES
    }
}
