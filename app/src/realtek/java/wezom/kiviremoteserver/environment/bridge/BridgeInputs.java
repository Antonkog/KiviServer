package wezom.kiviremoteserver.environment.bridge;

import android.annotation.TargetApi;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Build;
import android.view.KeyEvent;

import com.realtek.tv.Tv;
import com.wezom.kiviremoteserver.App;
import com.wezom.kiviremoteserver.common.Constants;
import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper;

import java.util.ArrayList;

import static com.wezom.kiviremoteserver.service.inputs.InputSourceHelper.INPUT_PORT.INPUT_SOURCE_NONE;

public class BridgeInputs {
    public void getPortsList(ArrayList<InputSourceHelper.INPUT_PORT> result, Context context) {
        String str = App.getProperty("ro.ota.modelname");
        boolean is2831 = "2831".equals(str.trim());
        String model = Build.MODEL;
        boolean is24inch = model != null && Build.MODEL.startsWith("24");

        result.add(InputSourceHelper.INPUT_PORT.INPUT_SOURCE_ATV);
        result.add(InputSourceHelper.INPUT_PORT.INPUT_SOURCE_CVBS);
//        result.add(InputSourceHelper.INPUT_PORT.INPUT_SOURCE_YPBPR);
//        result.add(InputSourceHelper.INPUT_PORT.INPUT_SOURCE_VGA);
        result.add(InputSourceHelper.INPUT_PORT.INPUT_SOURCE_HDMI);
        result.add(InputSourceHelper.INPUT_PORT.INPUT_SOURCE_HDMI2);
        if (!is2831 || !is24inch)
            result.add(InputSourceHelper.INPUT_PORT.INPUT_SOURCE_HDMI3);
        result.add(InputSourceHelper.INPUT_PORT.INPUT_SOURCE_DTV);
        if (!is2831) {
            result.add(InputSourceHelper.INPUT_PORT.INPUT_SOURCE_DVBS);
        } else {
            if (!is24inch)
                result.add(InputSourceHelper.INPUT_PORT.INPUT_SOURCE_YPBPR);
        }
        result.add(InputSourceHelper.INPUT_PORT.INPUT_SOURCE_DVBC);

//        TvInputManager inputManager = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
//        try {
//            if (inputManager != null && inputManager.getTvInputList() != null) {
//                for (TvInputInfo info : inputManager.getTvInputList()) {
//                    Log.e("TvInputManager", info.toString() +
//                            "::::" + inputManager.getInputState(info.getId()));
//                    InputSourceHelper.INPUT_PORT port = InputSourceHelper.INPUT_PORT.getPortByRealtekID(info.getId());
//                    port.setConnected(inputManager.getInputState(info.getId()) == TvInputManager.INPUT_STATE_CONNECTED);
//                    Log.e("port", port +":"+ port.isConnected());
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


    }

    public void changeInput(InputSourceHelper.INPUT_PORT inputPort, Context context) {
        String str = App.getProperty("ro.ota.modelname");
        String id = inputPort.getRealtekID();
        if (str != null && !str.isEmpty()) {
            str = str.trim();
            if ("2841".equals(str) && inputPort.getRealtekID2841() != null && !inputPort.getRealtekID2841().isEmpty()) {
                id = inputPort.getRealtekID2841();
            }
        }

        startTvInputs(getInputUri(id), context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Uri getInputUri(String SourceName) {

        if (SourceName.contains("atv") || SourceName.contains("dtv") || SourceName.contains("vga")) {
            return TvContract.buildChannelsUriForInput(SourceName);
        } else {
            return TvContract.buildChannelUriForPassthroughInput(SourceName);
//            return TvContract.buildChannelUriForPassthroughInput(SourceName);
        }
    }

    private void startTvInputs(Uri channelUri, Context context) {
        try {
            // Log.e("channelUri", channelUri.toString());
            Intent intent = new Intent(Intent.ACTION_VIEW, channelUri);
            ComponentName componentName = new ComponentName("com.android.tv", "com.android.tv.MainActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(componentName);
            context.startActivity(intent);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void changeProgram(int id, Context context) {
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Instrumentation instrumentation = new Instrumentation();
            instrumentation.sendKeyDownUpSync(CHANNEL.getChannelByID(id / 100).getCmd());
            instrumentation.sendKeyDownUpSync(CHANNEL.getChannelByID((id % 100) / 10).getCmd());
            instrumentation.sendKeyDownUpSync(CHANNEL.getChannelByID(id % 10).getCmd());
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
        }).start();

    }

    enum CHANNEL {
        ch0(0, KeyEvent.KEYCODE_0),
        ch1(1, KeyEvent.KEYCODE_1),
        ch2(2, KeyEvent.KEYCODE_2),
        ch3(3, KeyEvent.KEYCODE_3),
        ch4(4, KeyEvent.KEYCODE_4),
        ch5(5, KeyEvent.KEYCODE_5),
        ch6(6, KeyEvent.KEYCODE_6),
        ch7(7, KeyEvent.KEYCODE_7),
        ch8(8, KeyEvent.KEYCODE_8),
        ch9(9, KeyEvent.KEYCODE_9);

        int id;
        int cmd;

        CHANNEL(int id, int cmd) {
            this.id = id;
            this.cmd = cmd;
        }

        public int getCmd() {
            return cmd;
        }

        static CHANNEL getChannelByID(int id) {
            for (CHANNEL port : values()) {
                if (port.id == id) {
                    return port;
                }
            }
            return ch0;
        }
    }


    public int getCurrentTvInputSource() {
        final String s = App.getProperty(Constants.REALTEK_INPUT_SOURCE);
        InputSourceHelper.INPUT_PORT current = InputSourceHelper.INPUT_PORT.getPortByRealtekID(s);
        if (current != INPUT_SOURCE_NONE) {
            return current.getId();
        }
        return getSourceByRealtekLib();
    }

    public int getSourceByRealtekLib() {
        Tv mTV = new Tv();
        return mTV.GetActivatedSource(0);
    }

    public boolean isTV(int i) {
        return i == 1 || i == 2;
    }
}
