package com.wezom.kiviremoteserver.common;

/**
 * Created by andre on 13.06.2017.
 */

public class Constants {
    public static final boolean DEBUG = false;

    public static final String APPLICATION_UID = "app_uid";
    public static final String LAST_VOLUME_REALTEK = "last_volume_realtek";
    public static final String LAST_BACKLIGHT = "last_backlight";
    public static final String LAST_TREBLE = "treble_level";
    public static final String LAST_BASS = "bass_level";

    public static final int NO_VALUE = -1;
    public static final int SERV_REALTEK = 1;
    public static final int SERV_MSTAR = 0;

    public static final int SCROLL_VELOCITY_MS = 300;
    public static final int FIFTY = 50;
    public static final int REALTEK_SOUND_FIX = 50;  // that is for sound values on realtek
    // on mstar sound is not implemented. Sound values
    //REALTEK:
    //Standart
    //50 - 0
    //50 - 0
    //
    //Music
    //old70. - lib gives 8. now 58
    //old77 -  lib gives 11   now 61
    //
    //Film
    //old57 -3   lib gives - now 53
    //old70 -8  lib gives - now 58
    //realtek chanels
    public static final String SCREEN = "screen";
    public static final String MODEL = "model";
    public static final String BOARD = "board";
    public static final String PANEL = "panel";
    public static final String DEVICE = "device";
    public static final String NAME = "name";
    public static final String INCREMENTAL = "incremental";
    public static final String REALTEK_INPUT_SOURCE = "persist.sys.current_input";
    public static final String SOURCE_DVB_T = "com.realtek.dtv/.tvinput.DTVTvInputService/HW33685505";
    public static final String SOURCE_DVB_C = "com.realtek.dtv/.tvinput.DTVTvInputService/HW33685504";
    public static final String SOURCE_DVB_S = "com.realtek.dtv/.tvinput.DTVTvInputService/HW33685506";
    public static final String SOURCE_ATV = "com.realtek.tv.atv/.atvinput.AtvInputService/HW33619968";
    public static final String SOURCE_AV = "com.realtek.tv.avtvinput/.AVTvInputService/HW50593792";
    public static final String SOURCE_YPBPR = "com.realtek.tv.ypptvinput/.YPPTvInputService/HW101056512";
    public static final String SOURCE_HDMI1 = "com.realtek.tv.hdmitvinput/.HDMITvInputService/HW151519232";
    public static final String SOURCE_HDMI2 = "com.realtek.tv.hdmitvinput/.HDMITvInputService/HW151519488";
    public static final String SOURCE_HDMI3 = "com.realtek.tv.hdmitvinput/.HDMITvInputService/HW151519744";
    public static final String SOURCE_VGA = "com.realtek.tv.vgatvinput/.VGATvInputService/HW117899264";
}
