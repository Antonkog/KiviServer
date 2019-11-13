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
        REQUEST_IMG_BY_IDS,
        SHOW_OR_HIDE_ASPECT,
        REQUEST_INITIAL,
        REQUEST_INITIAL_II,
        LAUNCH_APP,
        LAUNCH_CHANNEL,
        CHANGE_INPUT,
        LAUNCH_RECOMMENDATION,
        LAUNCH_FAVORITE,
        PLAYER_ACTION,
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
        TV_PLAYER_ACTION,
        ASPECT,
        INITIAL,
        INITIAL_II,
        IMG_BY_IDS,
        APPS,
        LAUNCH_PLAYER,
        CHANGE_STATE,
        SEEK_TO,
        LAST_REQUEST_ERROR,
        LAST_REQUEST_OK
    }

}
