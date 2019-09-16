package com.wezom.kiviremoteserver.service.inputs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Base64;

import com.wezom.kiviremoteserver.App;
import com.wezom.kiviremoteserver.R;
import com.wezom.kiviremoteserver.common.Constants;
import com.wezom.kiviremoteserver.common.extensions.ViewExtensionsKt;
import com.wezom.kiviremoteserver.environment.EnvironmentInputsHelper;
import com.wezom.kiviremoteserver.interfaces.DriverValue;
import com.wezom.kiviremoteserver.net.server.model.Input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import io.reactivex.Single;
import timber.log.Timber;


/**
 * Created by s.gudym on 18.12.2017.
 */

public class InputSourceHelper {
    private static String TAG = InputSourceHelper.class.getSimpleName();


    public static List<INPUT_PORT> getPortsList(Context context) {
        ArrayList<INPUT_PORT> result = new ArrayList<>();
        new EnvironmentInputsHelper().getPortsList(result, context);
        Collections.sort(result, (o1, o2) -> -o1.weight + o2.weight);
        return result;
    }

    public void changeInput(int inputID, Context context) {
        new EnvironmentInputsHelper().changeInput(INPUT_PORT.getPortByID(inputID), context);
    }

    // public static final String SOURCE_REALTEK_9_HDMI1 = "com.realtek.tv.passthrough/.hdmiinput.HDMITvInputService/HW151519232";
    //    public static final String SOURCE_REALTEK_9_HDMI2 = "com.realtek.tv.passthrough/.hdmiinput.HDMITvInputService/HW151519488";
    //    public static final String SOURCE_REALTEK_9_HDMI3 = "com.realtek.tv.passthrough/.hdmiinput.HDMITvInputService/HW151519744";
    //    public static final String SOURCE_REALTEK_9_AV = "com.realtek.tv.passthrough/.avinput.AVTvInputService/HW50593792";
    //    public static final String SOURCE_REALTEK_9_VGA = "com.realtek.tv.passthrough/.vgainput.VGATvInputService/HW117899264";
    //    public static final String SOURCE_REALTEK_9_YPbPr = "com.realtek.tv.passthrough/.yppinput.YPPTvInputService/HW101056512";
    //    public static final String SOURCE_REALTEK_9_ATV = "com.realtek.tv.atv/.atvinput.AtvInputService/HW33619968";
    //    public static final String SOURCE_REALTEK_9_DVB_C = "com.realtek.dtv/.tvinput.DTVTvInputService/HW33685504";
    //    public static final String SOURCE_REALTEK_9_DVB_T = "com.realtek.dtv/.tvinput.DTVTvInputService/HW33685505";
    //    public static final String SOURCE_REALTEK_9_DVB_S = "com.realtek.dtv/.tvinput.DTVTvInputService/HW33685506";
    public enum INPUT_PORT {
        INPUT_SOURCE_VGA(0, "vga", R.string.vga, R.drawable.ic_kivi_input_icons_05, 10, Constants.SOURCE_VGA, Constants.SOURCE_VGA, Constants.SOURCE_REALTEK_9_VGA),//ic_settings_input_component_24dp
        INPUT_SOURCE_ATV(1, "atv", R.string.atv, R.drawable.ic_atv, 60, Constants.SOURCE_ATV, Constants.SOURCE_ATV, Constants.SOURCE_REALTEK_9_ATV),//ic_settings_input_antenna_24dp
        INPUT_SOURCE_CVBS(2, "av", R.string.av, R.drawable.ic_kivi_input_icons_05, 70, Constants.SOURCE_AV, Constants.SOURCE_AV, Constants.SOURCE_REALTEK_9_AV),//ic_settings_input_component_24dp
        INPUT_SOURCE_CVBS2(3, "cvbs2", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_CVBS3(4, "cvbs3", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_CVBS4(5, "cvbs4", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_CVBS5(6, "cvbs5", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_CVBS6(7, "cvbs6", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_CVBS7(8, "cvbs7", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_CVBS8(9, "cvbs8", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_CVBS_MAX(10, "cvbs_max", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_SVIDEO(11, "svideo", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_SVIDEO2(12, "svideo2", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_SVIDEO3(13, "svideo3", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_SVIDEO4(14, "svideo4", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_SVIDEO_MAX(15, "svideo_max", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_YPBPR(16, "ypbpr", R.string.ypbpr, R.drawable.ic_kivi_input_icons_05, 10, Constants.SOURCE_YPBPR, Constants.SOURCE_YPBPR, Constants.SOURCE_REALTEK_9_YPbPr),//ic_settings_input_component_24dp
        INPUT_SOURCE_YPBPR2(17, "ypbpr2", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_YPBPR3(18, "ypbpr3", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_YPBPR_MAX(19, "ypbpr_max", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_SCART(20, "scart", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_SCART2(21, "scart2", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_SCART_MAX(22, "scart_max", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_HDMI(23, "hdmi", R.string.hdmi, R.drawable.ic_kivi_input_icons_02, 85, Constants.SOURCE_HDMI1, Constants.SOURCE_HDMI3, Constants.SOURCE_REALTEK_9_HDMI1),//ic_settings_input_hdmi_24dp
        INPUT_SOURCE_HDMI2(24, "hdmi2", R.string.hdmi2, R.drawable.ic_kivi_input_icons_03, 84, Constants.SOURCE_HDMI2, Constants.SOURCE_HDMI2, Constants.SOURCE_REALTEK_9_HDMI2),//ic_settings_input_hdmi_24dp
        INPUT_SOURCE_HDMI3(25, "hdmi3", R.string.hdmi3, R.drawable.ic_kivi_input_icons_04, 83, Constants.SOURCE_HDMI3, Constants.SOURCE_HDMI1, Constants.SOURCE_REALTEK_9_HDMI3),//ic_settings_input_hdmi_24dp
        INPUT_SOURCE_HDMI4(26, "hdmi4", R.string.hdmi4, R.drawable.ic_kivi_input_icons_04, 82),//ic_settings_input_hdmi_24dp
        INPUT_SOURCE_HDMI_MAX(27, "hdmi_max", R.string.hdmi, R.drawable.ic_kivi_input_icons_04, 81),//ic_settings_input_hdmi_24dp
        INPUT_SOURCE_DTV(28, "dtv", R.string.dtv, R.drawable.ic_kivi_input_icons_13, 100, Constants.SOURCE_DVB_T, Constants.SOURCE_DVB_T, Constants.SOURCE_REALTEK_9_DVB_T),//ic_settings_input_antenna_24dp
        INPUT_SOURCE_DVI(29, "dvi", R.string.dvi, R.drawable.ic_kivi_input_icons_13, 10),//ic_settings_input_antenna_24dp
        INPUT_SOURCE_DVI2(30, "dvi2", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_DVI3(31, "dvi3", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_DVI4(32, "dvi4", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_DVI_MAX(33, "dvi_max", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_STORAGE(34, "storage", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_KTV(35, "ktv", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_JPEG(36, "jpeg", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_DTV2(37, "dtv2", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_STORAGE2(38, "storege2", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_DIV3(39, "div3", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_SCALER_OP(40, "scaler_op", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_RUV(41, "ruv", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_VGA2(42, "vga2", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_VGA3(43, "vga3", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_NUM(44, "num", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_NONE(45, "none", R.string.app_name, R.drawable.ic_av, 10),
        INPUT_SOURCE_DVBS(46, "dvbs", R.string.dvbs, R.drawable.ic_dvb_s, 20, Constants.SOURCE_DVB_S, Constants.SOURCE_DVB_S, Constants.SOURCE_REALTEK_9_DVB_S),
        INPUT_SOURCE_DVBC(47, "dvbс", R.string.dvbc, R.drawable.ic_dvb_c, 20, Constants.SOURCE_DVB_C, Constants.SOURCE_DVB_C, Constants.SOURCE_REALTEK_9_DVB_C);

        private int id;
        private String baseName;
        @StringRes
        private int nameResoucre;
        @DrawableRes
        int drawable;
        int weight;
        String realtekID;
        String realtekID2841;
        String realtekID2851;
        boolean isConnected;

        INPUT_PORT(int id, String baseName, @StringRes int visibleName, @DrawableRes int res, int weight, String realtekID, String realtekID2841, String realtekID2851) {
            this(id, baseName, visibleName, res, weight);//there was error hdmi1-3
            this.realtekID2841 = realtekID2841;
            this.realtekID2851 = realtekID2851;
            this.realtekID = realtekID;
        }

//        INPUT_PORT(int id, String baseName, @StringRes int visibleName, @DrawableRes int res, int weight, String realtekID) {
//            this(id, baseName, visibleName, res, weight);
//            this.realtekID = realtekID;
//        }

        INPUT_PORT(int id, String baseName, @StringRes int visibleName, @DrawableRes int res, int weight) {
            this.id = id;
            this.baseName = baseName;
            this.nameResoucre = visibleName;
            drawable = res;
            this.weight = weight;
        }


        public static INPUT_PORT getInstance() {
            return INPUT_SOURCE_NONE;
        }

        public void setConnected(boolean connected) {
            isConnected = connected;
        }

        public boolean isConnected() {
            return isConnected;
        }


        @Nullable
        public String getRealtekID(String str) {
            if (str != null && !str.isEmpty()) {
                str = str.trim();
                if ("2841".equals(str)) {
                    return realtekID2841;
                } else if ("2851".equals(str)
                        || "2842P533".equals(str)
                        || "2842P535".equals(str)
                        || "2842P735".equals(str)) {
                    return realtekID2851;
                }
            }
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

        public static INPUT_PORT getPortByID(int id) {
            for (INPUT_PORT port : values()) {
                if (port.id == id) {
                    return port;
                }
            }
            return INPUT_SOURCE_NONE;
        }

        public static INPUT_PORT getPortByRealtekID(String id) { //todo: moke fix
            String str = App.getProperty("ro.ota.modelname");
            boolean is2841 = "2841".equals(str.trim());
            boolean is2851 = "2851".equals(str.trim()) ||
                    "2842P533".equals(str.trim()) ||
                    "2842P735".equals(str.trim());
            if (is2841) {
                for (INPUT_PORT port : values()) {
                    if (port.realtekID2841 != null && port.realtekID2841.equals(id)) {
                        return port;
                    }
                }
            } else if (is2851) {
                for (INPUT_PORT port : values()) {
                    if (port.realtekID2851 != null && port.realtekID2851.equals(id)) {
                        return port;
                    }
                }
            } else {
                for (INPUT_PORT port : values()) {
                    if (port.realtekID != null && port.realtekID.equals(id)) {
                        return port;
                    }
                }
            }

            return INPUT_SOURCE_NONE;
        }

        public int getId() {
            return id;
        }

        public String getStringId() {
            return id + "";
        }

        public int getNameResource() {
            return nameResoucre;
        }

        public int getDrawable() {
            return drawable;
        }
    }

    public static List<DriverValue> getAsDriverList(Context context) {
        List<InputSourceHelper.INPUT_PORT> inputs = getPortsList(context);
        LinkedList<DriverValue> linkedList = new LinkedList<>();

        int currentPort = new EnvironmentInputsHelper().getCurrentTvInputSource();

        for (int i = 0; i < inputs.size(); i++) {
            InputSourceHelper.INPUT_PORT temp = inputs.get(i);

            linkedList.add(new DriverValue(INPUT_PORT.class.getSimpleName(),
                    context.getResources().getString(temp.getNameResource()),
                    temp.getStringId()
                    , temp.getId(),
                    currentPort == temp.getId()));
        }
        return linkedList;
    }


    public static List<Input> getAsInputs(Context context) {
        List<InputSourceHelper.INPUT_PORT> inputs = getPortsList(context);
        Set<Input> set = new TreeSet<>();

        int currentPort = new EnvironmentInputsHelper().getCurrentTvInputSource();

        for (int i = 0; i < inputs.size(); i++) {
            InputSourceHelper.INPUT_PORT temp = inputs.get(i);
            try {
                byte[] iconBytes;
                Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), temp.drawable, null);
                if (drawable != null) {
                    int width = drawable.getIntrinsicWidth();
                    int height = drawable.getIntrinsicHeight();

                    iconBytes = ViewExtensionsKt.getIconBytes(context, ViewExtensionsKt.dpToPx(context, Constants.INPUT_ICON_WH), ViewExtensionsKt.dpToPx(context, Constants.INPUT_ICON_WH), drawable);

                    String byteString = Base64.encodeToString(iconBytes, Base64.DEFAULT);

                    set.add(new Input()
                            .addPortName(context.getResources().getString(temp.getNameResource()))
                            .addActive(currentPort == temp.getId())
                            .addInputIcon(byteString)
                            .addPortNum(temp.getId()));

                } else {
                    set.add(new Input()
                            .addPortName(context.getResources().getString(temp.getNameResource()))
                            .addActive(currentPort == temp.getId())
                            .addPortNum(temp.getId()));
                }
            } catch (Exception e) {
                Timber.e(e);
                set.add(new Input()
                        .addPortName(context.getResources().getString(temp.getNameResource()))
                        .addActive(currentPort == temp.getId())
                        .addPortNum(temp.getId()));
            }
        }
        return new LinkedList<>(set);
    }


    public static Single<List<Input>> getInputsSingle(Context context) {
        return Single.create(emitter -> {

            Timber.e("getAsInputs started");
            try {
                List<Input> inputs = getAsInputs(context);
                emitter.onSuccess(inputs);
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

}
