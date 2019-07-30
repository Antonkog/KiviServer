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
        SHOW_OR_HIDE_ASPECT,
        REQUEST_INITIAL,
        REQUEST_INITIAL_II,
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
        SET_VOLUME,
        VOICE_SEARCH,
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
//        RECONNECTION_NEED,
//        OK200,
//        CONN_ESTABLISHED,
//        COORDINATES,
        ASPECT,
        INITIAL,
        INITIAL_II,
        CHANNELS,
        INPUTS,
        APPS,
        FAVORITES,
        RECOMMENDATIONS,
        LAST_REQUEST_ERROR,
        LAST_REQUEST_OK
    }
}
