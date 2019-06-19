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
        @Deprecated SCROLL_TOP_TO_BOTTOM, //remote 1.1.14
        @Deprecated SCROLL_BOTTOM_TO_TOP,//remote  1.1.14
        REQUEST_APPS,
        REQUEST_ASPECT,
        REQUEST_INITIAL,
        REQUEST_CHANNELS,
        REQUEST_INPUTS,
        REQUEST_RECOMMENDATIONS,
        REQUEST_FAVORITES,
        LAUNCH_APP,
        LAUNCH_CHANNEL,
        CHANGE_INPUT,
        LAUNCH_RECOMMENDATION,
        LAUNCH_FAVORITE,
        REQUEST_VOLUME,
        PING,
        SWITCH_OFF,
        SCROLL, //for old version
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
        COORDINATES,
        ASPECT,
        INITIAL,
        CHANNELS,
        INPUTS,
        FAVORITES,
        RECOMMENDATIONS,
        LAST_REQUEST_ERROR,
        LAST_REQUEST_OK
    }
}
