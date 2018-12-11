package com.wezom.kiviremoteserver.environment;

import android.content.Context;

import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper;

import java.util.ArrayList;

import wezom.kiviremoteserver.environment.bridge.BridgeInputs;


public class EnvironmentInputsHelper {

    public void changeProgram(int id, Context context) {
        BridgeInputs bridgeInputs = new BridgeInputs();
        bridgeInputs.changeProgram(id, context);
    }


//    public void changeInputSource(int inpSource) {
//
////        int curIntSource = mTvCommonmanager.getCurrentTvInputSource();
////        if ((curIntSource >= TvCommonManager.INPUT_SOURCE_STORAGE)
////                && (curIntSource != TvCommonManager.INPUT_SOURCE_VGA2)
////                && (curIntSource != TvCommonManager.INPUT_SOURCE_VGA3)) {
////            Intent source_switch_from_storage = new Intent("source.switch.from.storage");
////            mContext.sendBroadcast(source_switch_from_storage);
////            executePreviousTask(inpSource);
////        } else {
////            new Thread(new Runnable() {
////                @Override
////                public void run() {
////                    updateSourceInputType(inpSource);
////                }
////            }).start();
////        }
//    }


    //    InputSourceHelper.DisplayPortInfo sDisplayPortInfo;
//
    public void getPortsList(ArrayList<InputSourceHelper.INPUT_PORT> result, Context context) {
        BridgeInputs bridgeInputs = new BridgeInputs();
        bridgeInputs.getPortsList(result, context);

    }

    public void changeInput(InputSourceHelper.INPUT_PORT inputPort, Context context) {
        BridgeInputs bridgeInputs = new BridgeInputs();
        bridgeInputs.changeInput(inputPort, context);

    }

    public int getCurrentTvInputSource() {
        BridgeInputs bridgeInputs = new BridgeInputs();
        return bridgeInputs.getCurrentTvInputSource();

    }

    public boolean isTV(int i) {
        BridgeInputs bridgeInputs = new BridgeInputs();
        return bridgeInputs.isTV(i);//i == 28 || i == 1
    }

    //private void executePreviousTask(final int inpSource) {
    //        new Thread(new Runnable() {
    //            @Override
    //            public void run() {
    //                int inputSource = inpSource;
    //                //
    //                int mTvSystem = 0;
    //                //
    //                mTvS3DManager.setDisplayFormatForUI(TvS3DManager.THREE_DIMENSIONS_DISPLAY_FORMAT_NONE);
    //
    //                DebugUtils.LogI(TAG, "startActivity SOURCE_CHANGE intent: inputSource = " + inputSource);
    //                if (inputSource == TvCommonManager.INPUT_SOURCE_ATV) {
    //                    if (TvCommonManager.TV_SYSTEM_ISDB == mTvSystem) {
    //                        TvIsdbChannelManager.getInstance().setAntennaType(
    //                                TvIsdbChannelManager.DTV_ANTENNA_TYPE_AIR);
    //                    }
    //                    if (TvCommonManager.TV_SYSTEM_ATSC == mTvSystem) {
    //                        AtscMainListChannelInformation info = TvAtscChannelManager.getInstance()
    //                                .getCurrentChannelInformation();
    //                        if (info != null) {
    //                            if (info.progId == CHANNELMANAGER_ATSC_ATV_ID) {
    //                                inputSource = TvCommonManager.INPUT_SOURCE_ATV;
    //                            }
    //                        }
    //                    }
    //                }
    //
    //                Intent intent = new Intent(MIntent.ACTION_START_TV_PLAYER);
    //                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    //                intent.putExtra("inputSrc", inputSource);
    //                intent.putExtra("inputAntennaType", mAntennaType);
    //                mContext.startActivity(intent);
    //                try {
    //                    Intent targetIntent;
    //                    targetIntent = new Intent("mstar.tvsetting.ui.intent.action.RootActivity");
    //                    targetIntent.putExtra("task_tag", "input_source_changed");
    //                    /* DO NOT remove on_change_source extra!, it will cause mantis:1088498. */
    //                    targetIntent.putExtra("no_change_source", true);
    //                    targetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    //                    mContext.startActivity(targetIntent);
    //
    //                } catch (Exception e) {
    //                    e.printStackTrace();
    //                }
    //            }
    //        }).start();
    //    }

    // private void updateSourceInputType(int inputSourceTypeIdex) {
    //        long ret = -1;
    //
    //        ContentValues vals = new ContentValues();
    //        vals.put("enInputSourceType", inputSourceTypeIdex);
    //        try {
    //            ret = mContext.getContentResolver().update(
    //                    Uri.parse("content://mstar.tv.usersetting/systemsetting"), vals, null, null);
    //        } catch (SQLException e) {
    //        }
    //        if (ret == -1) {
    //            DebugUtils.LogI(TAG, "update tbl_PicMode_Setting ignored");
    //        }
    //    }

    public interface InputObserver {
        void onHDMIConnected(String id);

        void onUSBConnected(String id);

        void onHDMIDisconnected(String id);

        void onUSBDisconnected(String id);
    }
}
