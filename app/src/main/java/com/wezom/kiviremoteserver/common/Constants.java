package com.wezom.kiviremoteserver.common;

/**
 * Created by andre on 13.06.2017.
 */

public class Constants {
    public static final boolean DEBUG = false;

    public static final String LAUNCHER_PACKAGE = "com.kivi.launcher_v2";
    public static final String LAUNCHER_SERVICE = "com.kivi.launcher_v2.services.StartContentService";
    public static final String PKG_SETUP_WIZARD = "com.hikeen.setupwizard";
    public static final String LAUNCHER_PREF_KEY = "pref_key_data";

    public static final String PREFERENCE_CATEGORY = "AbsManager";
    public static final String RECOMMENDATION_MANAGER = "recommendation";
    public static final String FAVORITES_MANAGER = "favorites";
    public static final String CHANNEL_MANAGER = "kiviTV";


    public static final String APPLICATION_UID = "app_uid";
    public static final String LAST_VOLUME = "last_volume";
    public static final String LAST_BACKLIGHT = "last_backlight";
    public static final String LAST_TREBLE = "treble_level";
    public static final String LAST_BASS = "bass_level";


    public final static String LOG_FILE_PREFIX = "KiviLogs";
    public final static String LOG_FILE_EXTENSION = ".txt";

    public static final int NO_VALUE = -1;
    public static final int SERV_REALTEK = 1;
    public static final int SERV_MSTAR = 0;
    public static final int APP_ICON_W = 160;
    public static final int APP_ICON_H = 90;
    public static final int INPUT_ICON_WH = 30;



    public static final int APPS_SENDING_DELAY = 300; // for some reason PM collecting old app's if asking immediately after package removed
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


    public static final String SOURCE_REALTEK_9_HDMI1 = "com.realtek.tv.passthrough/.hdmiinput.HDMITvInputService/HW151519744";
    public static final String SOURCE_REALTEK_9_HDMI2 = "com.realtek.tv.passthrough/.hdmiinput.HDMITvInputService/HW151519488";
    public static final String SOURCE_REALTEK_9_HDMI3 = "com.realtek.tv.passthrough/.hdmiinput.HDMITvInputService/HW151519232";
    public static final String SOURCE_REALTEK_9_AV = "com.realtek.tv.passthrough/.avinput.AVTvInputService/HW50593792";
    public static final String SOURCE_REALTEK_9_VGA = "com.realtek.tv.passthrough/.vgainput.VGATvInputService/HW117899264";
    public static final String SOURCE_REALTEK_9_YPbPr = "com.realtek.tv.passthrough/.yppinput.YPPTvInputService/HW101056512";
    public static final String SOURCE_REALTEK_9_ATV = "com.realtek.tv.atv/.atvinput.AtvInputService/HW33619968";
    public static final String SOURCE_REALTEK_9_DVB_C = "com.realtek.dtv/.tvinput.DTVTvInputService/HW33685504";
    public static final String SOURCE_REALTEK_9_DVB_T = "com.realtek.dtv/.tvinput.DTVTvInputService/HW33685505";
    public static final String SOURCE_REALTEK_9_DVB_S = "com.realtek.dtv/.tvinput.DTVTvInputService/HW33685506";


    //HDMI1：com.realtek.tv.passthrough/.hdmiinput.HDMITvInputService/HW151519232
//HDMI2：com.realtek.tv.passthrough/.hdmiinput.HDMITvInputService/HW151519488
//HDMI3：com.realtek.tv.passthrough/.hdmiinput.HDMITvInputService/HW151519744
//AV：   com.realtek.tv.passthrough/.avinput.AVTvInputService/HW50593792
//VGA：  com.realtek.tv.passthrough/.vgainput.VGATvInputService/HW117899264
//YPbPr：com.realtek.tv.passthrough/.yppinput.YPPTvInputService/HW101056512
//
//ATV:com.realtek.tv.atv/.atvinput.AtvInputService/HW33619968
//DVB-C:com.realtek.dtv/.tvinput.DTVTvInputService/HW33685504
//DVB-T:com.realtek.dtv/.tvinput.DTVTvInputService/HW33685505
//DVB-S:com.realtek.dtv/.tvinput.DTVTvInputService/HW33685506
}
