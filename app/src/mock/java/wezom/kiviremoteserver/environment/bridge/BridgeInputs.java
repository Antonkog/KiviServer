package wezom.kiviremoteserver.environment.bridge;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper;

import java.util.ArrayList;

public class BridgeInputs {


    public void changeInput(InputSourceHelper.INPUT_PORT inputPort, Context context) {
     //TODO Activity
             Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("inputSrc", context.getString(inputPort.getNameResource()));
        context.startActivity(intent);
    }

    DisplayPortInfo sDisplayPortInfo;

    public void getPortsList(ArrayList<InputSourceHelper.INPUT_PORT> result, Context context) {
        if (sDisplayPortInfo == null) {
            sDisplayPortInfo = new DisplayPortInfo();
            String MODEL = Build.MODEL;// 目前我们所有的电视都是"MStar Android TV",未来区分不同型号,比如"FD4951A-LU"
            String AUTHORITY = "model";
            Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/device_info");


            String selection = "device_model='" + MODEL + "'";
            //DebugUtils.LogI("MODEL_sDisplayPortInfo", MODEL);
            Cursor cursor = context.getContentResolver().query(CONTENT_URI,
                    new String[]{"display_port", "usb_amount", "real_port", "av_type", "ypbpr_type"},
                    selection, null, null);
            if (cursor == null) {
                return;
            }
            try {
                if (cursor.moveToFirst()) {
                    // DebugUtils.LogI("cursor_sDisplayPortInfo",cursor.getString(cursor.getColumnIndexOrThrow("display_port")));
                    // DebugUtils.LogI("cursor_sDisplayPortInfo",cursor.getString(cursor.getColumnIndexOrThrow("real_port")));
                    sDisplayPortInfo.setDisplayPort(cursor.getString(cursor.getColumnIndexOrThrow("display_port")));
                    sDisplayPortInfo.setRealPort(cursor.getString(cursor.getColumnIndexOrThrow("real_port")));
                    sDisplayPortInfo.setUsbCnt(cursor.getInt(cursor.getColumnIndexOrThrow("usb_amount")));
                    sDisplayPortInfo.setAvPortType(cursor.getInt(cursor.getColumnIndexOrThrow("av_type")));
                    sDisplayPortInfo.setYuvPortType(cursor.getInt(cursor.getColumnIndexOrThrow("ypbpr_type")));
                    sDisplayPortInfo.setMultiLine(false);
              //      Log.e("sDisplayPortInfo", sDisplayPortInfo.toString());
                    //sDisplayPortInfo.setMultiLine(cursor.getInt(cursor.getColumnIndexOrThrow("multiline")) == 1);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        for (String string : sDisplayPortInfo.displayPort.split("-")) {
            if (string != null) {
                string = string.trim();
                InputSourceHelper.INPUT_PORT port = InputSourceHelper.INPUT_PORT.getPortByBaseName(string);
                if (port != InputSourceHelper.INPUT_PORT.INPUT_SOURCE_NONE) {
                    result.add(port);
                }
            }
        }
    }

    public void changeProgram(int id, Context context) {
        //TODO Activity
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("channel", id);
        context.startActivity(intent);
    }

    public int getCurrentTvInputSource() {
        //TODO get current activity
        return 0;
    }

    public boolean isTV(int i) {
        return i == 28 || i == 1;
    }


    private static class DisplayPortInfo {
        private String displayPort;
        private String realPort;
        private int usbCnt;
        private int avPortType;
        private boolean multiLine;

        @Override
        public String toString() {
            return "DisplayPortInfo{" +
                    "displayPort='" + displayPort + '\'' +
                    ", realPort='" + realPort + '\'' +
                    ", usbCnt=" + usbCnt +
                    ", avPortType=" + avPortType +
                    ", multiLine=" + multiLine +
                    ", yuvPortType=" + yuvPortType +
                    '}';
        }

        private int yuvPortType;

        public void setDisplayPort(String displayPort) {
            this.displayPort = displayPort;
        }

        public void setRealPort(String realPort) {
            this.realPort = realPort;
        }

        public void setUsbCnt(int usbCnt) {
            this.usbCnt = usbCnt;
        }

        public void setAvPortType(int avPortType) {
            this.avPortType = avPortType;
        }

        public void setMultiLine(boolean multiLine) {
            this.multiLine = multiLine;
        }

        public void setYuvPortType(int yuvPortType) {
            this.yuvPortType = yuvPortType;
        }
    }
}
