package com.wezom.kiviremoteserver.service.inputs;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.wezom.kiviremoteserver.R;
import com.wezom.kiviremoteserver.common.Constants;
import com.wezom.kiviremoteserver.environment.EnvironmentInputsHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by s.gudym on 18.12.2017.
 */

public class InputSourceHelper {
    private static String TAG = InputSourceHelper.class.getSimpleName();


    public List<INPUT_PORT> getPortsList(Context context) {
        ArrayList<INPUT_PORT> result = new ArrayList<>();
        new EnvironmentInputsHelper().getPortsList(result, context);
        Collections.sort(result, (o1, o2) -> -o1.weight + o2.weight);
        return result;
    }

    public void changeInput(int inputID, Context context) {
        new EnvironmentInputsHelper().changeInput(INPUT_PORT.getPortByID(inputID), context);
    }

    public enum INPUT_PORT {
        INPUT_SOURCE_VGA(0, "vga", R.string.vga, R.drawable.ic_ser_fm, 10, Constants.SOURCE_VGA),//ic_settings_input_component_24dp
        INPUT_SOURCE_ATV(1, "atv", R.string.atv, R.drawable.ic_ser_tv, 60, Constants.SOURCE_ATV),//ic_settings_input_antenna_24dp
        INPUT_SOURCE_CVBS(2, "av", R.string.av, R.drawable.ic_ser_fm, 70, Constants.SOURCE_AV),//ic_settings_input_component_24dp
        INPUT_SOURCE_CVBS2(3, "cvbs2", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_CVBS3(4, "cvbs3", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_CVBS4(5, "cvbs4", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_CVBS5(6, "cvbs5", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_CVBS6(7, "cvbs6", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_CVBS7(8, "cvbs7", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_CVBS8(9, "cvbs8", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_CVBS_MAX(10, "cvbs_max", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_SVIDEO(11, "svideo", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_SVIDEO2(12, "svideo2", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_SVIDEO3(13, "svideo3", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_SVIDEO4(14, "svideo4", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_SVIDEO_MAX(15, "svideo_max", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_YPBPR(16, "ypbpr", R.string.ypbpr, R.drawable.ic_ser_fm, 10, Constants.SOURCE_YPBPR),//ic_settings_input_component_24dp
        INPUT_SOURCE_YPBPR2(17, "ypbpr2", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_YPBPR3(18, "ypbpr3", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_YPBPR_MAX(19, "ypbpr_max", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_SCART(20, "scart", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_SCART2(21, "scart2", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_SCART_MAX(22, "scart_max", R.string.app_name, R.drawable.ic_settings_time, 10),

        INPUT_SOURCE_HDMI(23, "hdmi", R.string.hdmi, R.drawable.ic_ser_hdmi, 85, Constants.SOURCE_HDMI1, Constants.SOURCE_HDMI3),//ic_settings_input_hdmi_24dp
        INPUT_SOURCE_HDMI2(24, "hdmi2", R.string.hdmi2, R.drawable.ic_ser_hdmi, 84, Constants.SOURCE_HDMI2),//ic_settings_input_hdmi_24dp
        INPUT_SOURCE_HDMI3(25, "hdmi3", R.string.hdmi3, R.drawable.ic_ser_hdmi, 83, Constants.SOURCE_HDMI3, Constants.SOURCE_HDMI1),//ic_settings_input_hdmi_24dp
        INPUT_SOURCE_HDMI4(26, "hdmi4", R.string.hdmi4, R.drawable.ic_ser_hdmi, 82),//ic_settings_input_hdmi_24dp
        INPUT_SOURCE_HDMI_MAX(27, "hdmi_max", R.string.app_name, R.drawable.ic_ser_hdmi, 81),//ic_settings_input_hdmi_24dp
        INPUT_SOURCE_DTV(28, "dtv", R.string.dtv, R.drawable.ic_ser_tv, 100, Constants.SOURCE_DVB_T),//ic_settings_input_antenna_24dp
        INPUT_SOURCE_DVI(29, "dvi", R.string.dvi, R.drawable.ic_ser_tv, 10),//ic_settings_input_antenna_24dp
        INPUT_SOURCE_DVI2(30, "dvi2", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_DVI3(31, "dvi3", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_DVI4(32, "dvi4", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_DVI_MAX(33, "dvi_max", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_STORAGE(34, "storage", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_KTV(35, "ktv", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_JPEG(36, "jpeg", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_DTV2(37, "dtv2", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_STORAGE2(38, "storege2", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_DIV3(39, "div3", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_SCALER_OP(40, "scaler_op", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_RUV(41, "ruv", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_VGA2(42, "vga2", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_VGA3(43, "vga3", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_NUM(44, "num", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_NONE(44, "none", R.string.app_name, R.drawable.ic_settings_time, 10),
        INPUT_SOURCE_DVBS(46, "dvbs", R.string.dvbs, R.drawable.ic_dvb_s, 20, Constants.SOURCE_DVB_S),
        INPUT_SOURCE_DVBC(47, "dvbс", R.string.dvbc, R.drawable.ic_dvb_c, 20, Constants.SOURCE_DVB_C);

        private int id;
        private String baseName;
        @StringRes
        private int nameResoucre;
        @DrawableRes
        int drawable;
        int weight;
        String realtekID;
        String realtekID2841;
        boolean isConnected;
        INPUT_PORT(int id, String baseName, @StringRes int visibleName, @DrawableRes int res, int weight, String realtekID, String realtekID2841) {
            this(id, baseName, visibleName, res, weight, realtekID2841);
            this.realtekID2841 = realtekID2841;
        }

        INPUT_PORT(int id, String baseName, @StringRes int visibleName, @DrawableRes int res, int weight, String realtekID) {
            this(id, baseName, visibleName, res, weight);
            this.realtekID = realtekID;
        }

        INPUT_PORT(int id, String baseName, @StringRes int visibleName, @DrawableRes int res, int weight) {
            this.id = id;
            this.baseName = baseName;
            this.nameResoucre = visibleName;
            drawable = res;
            this.weight = weight;
        }

        public void setConnected(boolean connected) {
            isConnected = connected;
        }

        public boolean isConnected() {
            return isConnected;
        }

        @Nullable
        public String getRealtekID2841() {
            return realtekID2841;
        }

        @Nullable
        public String getRealtekID() {
            return realtekID;
        }

        public static INPUT_PORT getPortByBaseName(String name) {
            for (INPUT_PORT port : values()) {
                if (port.baseName.equals(name)) {
                    return port;
                }
            }
            return INPUT_SOURCE_NONE;
        }

        static INPUT_PORT getPortByID(int id) {
            for (INPUT_PORT port : values()) {
                if (port.id == id) {
                    return port;
                }
            }
            return INPUT_SOURCE_NONE;
        }

        public static INPUT_PORT  getPortByRealtekID(String id){
            for (INPUT_PORT port : values()) {
                if (port.realtekID!= null && port.realtekID.equals(id)) {
                    return port;
                }
            }
            return INPUT_SOURCE_NONE;
        }
        public int getId() {
            return id;
        }

        public int getNameResource() {
            return nameResoucre;
        }

        public int getDrawable() {
            return drawable;
        }
    }


}
