package wezom.kiviremoteserver.environment.bridge;

import android.annotation.TargetApi;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.tv.TvContract;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.os.Build;
import android.view.KeyEvent;

import com.realtek.tv.Tv;
import com.wezom.kiviremoteserver.App;
import com.wezom.kiviremoteserver.environment.EnvironmentInputsHelper;
import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper;

import java.util.ArrayList;

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

    TvInputManager inputManager;
    TvInputManager.TvInputCallback callback;
    BroadcastReceiver mUsbReceiver;

    public void subscribe(Context context, EnvironmentInputsHelper.InputObserver inputObserver) {
//        inputManager = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
//        try {
//            if (inputManager != null && inputManager.getTvInputList() != null) {
//                for (TvInputInfo info : inputManager.getTvInputList()) {
//                    Log.e("TvInputManager", info.toString() +
//                            " = " + inputManager.getInputState(info.getId()));
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        callback = new TvInputManager.TvInputCallback() {
//            @Override
//            public void onInputStateChanged(String inputId, int state) {
//                Log.e("inputManager", "");
//                super.onInputStateChanged(inputId, state);
//            }
//
//            @Override
//            public void onInputAdded(String inputId) {
//                Log.e("inputManager", "onInputAdded¬");
//                super.onInputAdded(inputId);
//            }
//
//            @Override
//            public void onInputRemoved(String inputId) {
//                Log.e("inputManager", "onInputRemoved");
//                super.onInputRemoved(inputId);
//            }
//
//            @Override
//            public void onInputUpdated(String inputId) {
//                Log.e("inputManager", "onInputUpdated");
//                super.onInputUpdated(inputId);
//            }
//
//            @Override
//            public void onTvInputInfoUpdated(TvInputInfo inputInfo) {
//                Log.e("inputManager", "onTvInputInfoUpdated");
//                super.onTvInputInfoUpdated(inputInfo);
//            }
//        };
//        inputManager.registerCallback(callback, new Handler(new Handler.Callback() {
//            @Override
//            public boolean handleMessage(Message msg) {
//                Log.e("inputManager", "handleMessage");
//                return false;
//            }
//        }));

//        mUsbReceiver = new BroadcastReceiver() {
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getAction();
//                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//                Log.e("inputManager", "usb " + action + "  : " + device);
//            }
//        };
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
//        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//        context.registerReceiver(mUsbReceiver, filter);

    }
    // UsbDevice[mName=/dev/bus/usb/001/007,mVendorId=2855,mProductId=27032,mClass=0,mSubclass=0,mProtocol=0,mManufacturerName=Generic,mProductName=Mass Storage,mVersion=2.0,mSerialNumber=5AC8D4B0,mConfigurations=[
    //    UsbConfiguration[mId=1,mName=null,mAttributes=128,mMaxPower=100,mInterfaces=[
    //    UsbInterface[mId=0,mAlternateSetting=0,mName=null,mClass=8,mSubclass=6,mProtocol=80,mEndpoints=[
    //    UsbEndpoint[mAddress=1,mAttributes=2,mMaxPacketSize=512,mInterval=0]
    //    UsbEndpoint[mAddress=130,mAttributes=2,mMaxPacketSize=512,mInterval=0]]]]

    // UsbDevice[mName=/dev/bus/usb/001/008,mVendorId=6421,mProductId=4113,mClass=0,mSubclass=0,mProtocol=0,mManufacturerName=123 COM,mProductName=Smart Control,mVersion=1.16,mSerialNumber=null,mConfigurations=[
    //    UsbConfiguration[mId=1,mName=null,mAttributes=160,mMaxPower=100,mInterfaces=[
    //    UsbInterface[mId=0,mAlternateSetting=0,mName=null,mClass=1,mSubclass=1,mProtocol=0,mEndpoints=[]
    //    UsbInterface[mId=1,mAlternateSetting=0,mName=null,mClass=1,mSubclass=2,mProtocol=0,mEndpoints=[]
    //    UsbInterface[mId=1,mAlternateSetting=1,mName=null,mClass=1,mSubclass=2,mProtocol=0,mEndpoints=[
    //    UsbEndpoint[mAddress=135,mAttributes=5,mMaxPacketSize=32,mInterval=1]]
    //    UsbInterface[mId=2,mAlternateSetting=0,mName=null,mClass=3,mSubclass=0,mProtocol=1,mEndpoints=[
    //    UsbEndpoint[mAddress=129,mAttributes=3,mMaxPacketSize=32,mInterval=8]]
    //    UsbInterface[mId=3,mAlternateSetting=0,mName=null,mClass=3,mSubclass=0,mProtocol=2,mEndpoints=[
    //    UsbEndpoint[mAddress=130,mAttributes=3,mMaxPacketSize=32,mInterval=8]]
    //    UsbInterface[mId=4,mAlternateSetting=0,mName=null,mClass=3,mSubclass=0,mProtocol=0,mEndpoints=[
    //    UsbEndpoint[mAddress=131,mAttributes=3,mMaxPacketSize=32,mInterval=8]]]]

    public void unSubscribe(Context context) {
        //      context.unregisterReceiver(mUsbReceiver);
        //     inputManager.unregisterCallback(callback);
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
        Tv mTV = new Tv();
        return mTV.GetActivatedSource(0);
    }

    public boolean isTV(int i) {
        return i == 1 || i == 2;
    }
}
