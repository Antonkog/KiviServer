package com.wezom.kiviremoteserver.service.aspect;//package launcher.kivi.com.kivilauncher.utils;
//
//import android.app.Activity;
//import android.app.ActivityManager;
//import android.app.AlarmManager;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.support.v7.app.AlertDialog;
//import android.util.Log;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.mstar.android.tv.TvAtscChannelManager;
//import com.mstar.android.tv.TvChannelManager;
//import com.mstar.android.tv.TvCommonManager;
//import com.mstar.android.tv.TvParentalControlManager;
//import com.mstar.android.tv.TvPvrManager;
//import com.mstar.android.tvapi.common.vo.TvTypeInfo;
//
//import java.util.Arrays;
//
///**
// * Created by sergiigudym on 3/30/18.
// */
//
//
//public class Utility {
//    private static final String TAG = "Utility";
//
//    private static Context sContext = null;
//
//    private static int[] sRouteTable = null;
//
//    private static String[] sRouteTableName = null;
//
//    private static final int DO_CHANNEL_UP = 1;
//
//    private static final int DO_CHANNEL_DOWN = 2;
//
//    private static final int DO_CHANNEL_RETURN = 3;
//
//    private static final int DO_CHANNEL_SELECT = 4;
//
//    /**
//     * Parental guidance spec for Singapore, Australia and Default
//     */
//    private static final int[] sParentalValueDefault = new int[] {
//            0, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18
//    };
//
//    private static final int[] sParentalValueSingapore = new int[] {
//            0, 4, 5, 8, 14, 17, 19
//    };
//
//    private static final int[] sParentalValueAustralian = new int[] {
//            0, 1, 5, 7, 9, 11, 13, 15, 17, 18
//    };
//
//    private static final int AUSTRALIAN_PARENTAL_VALUE_BLOCK_ALL_VALUE = 1;
//
//    /**
//     * TvSystem
//     *
//     * @see com.mstar.android.tv.TvCommonManager#TV_SYSTEM_DVBT
//     * @see com.mstar.android.tv.TvCommonManager#TV_SYSTEM_DVBC
//     * @see com.mstar.android.tv.TvCommonManager#TV_SYSTEM_DVBS
//     * @see com.mstar.android.tv.TvCommonManager#TV_SYSTEM_DVBT2
//     * @see com.mstar.android.tv.TvCommonManager#TV_SYSTEM_DVBS2
//     * @see com.mstar.android.tv.TvCommonManager#TV_SYSTEM_DTMB
//     * @see com.mstar.android.tv.TvCommonManager#TV_SYSTEM_ATSC
//     * @see com.mstar.android.tv.TvCommonManager#TV_SYSTEM_ISDB
//     */
//    private static int sTvSystem = -1;
//
//    public static final int SCROLL_DIRECTION_UP = 1;
//
//    public static final int SCROLL_DIRECTION_DOWN = 2;
//
//    public void Utility() {
//
//    }
//
//
//
//    public static int getCurrentTvSystem() {
//        if (sTvSystem < 0) {
//            sTvSystem = TvCommonManager.getInstance().getCurrentTvSystem();
//        }
//        return sTvSystem;
//    }
//
//    public static boolean isATSC() {
//        if (sTvSystem > 0) {
//            return TvCommonManager.TV_SYSTEM_ATSC == sTvSystem;
//        }
//        return TvCommonManager.TV_SYSTEM_ATSC == getCurrentTvSystem();
//    }
//
//    public static boolean isISDB() {
//        if (sTvSystem > 0) {
//            return TvCommonManager.TV_SYSTEM_ISDB == sTvSystem;
//        }
//        return TvCommonManager.TV_SYSTEM_ISDB == getCurrentTvSystem();
//    }
//
//    public static boolean isDVBT() {
//        boolean ret = false;
//        int currentSystem = getCurrentTvSystem();
//        if ((TvCommonManager.TV_SYSTEM_ATSC != currentSystem)
//                && (TvCommonManager.TV_SYSTEM_ISDB != currentSystem)) {
//            int currentRoute = TvChannelManager.TV_ROUTE_NONE;
//            TvTypeInfo tvinfo = TvCommonManager.getInstance().getTvInfo();
//            int routeIdx = TvChannelManager.getInstance().getCurrentDtvRouteIndex();
//            currentRoute = tvinfo.routePath[routeIdx];
//            if ((TvChannelManager.TV_ROUTE_DVBT == currentRoute)
//                    || (TvChannelManager.TV_ROUTE_DVBT2 == currentRoute)) {
//                ret = true;
//            }
//        }
//        return ret;
//    }
//
//    public static boolean isDVBS() {
//        boolean ret = false;
//        int currentSystem = getCurrentTvSystem();
//        if ((TvCommonManager.TV_SYSTEM_ATSC != currentSystem)
//                && (TvCommonManager.TV_SYSTEM_ISDB != currentSystem)) {
//            int currentRoute = TvChannelManager.TV_ROUTE_NONE;
//            TvTypeInfo tvinfo = TvCommonManager.getInstance().getTvInfo();
//            int routeIdx = TvChannelManager.getInstance().getCurrentDtvRouteIndex();
//            currentRoute = tvinfo.routePath[routeIdx];
//            if ((TvChannelManager.TV_ROUTE_DVBS == currentRoute)
//                    || (TvChannelManager.TV_ROUTE_DVBS2 == currentRoute)) {
//                ret = true;
//            }
//        }
//        return ret;
//    }
//
//    public static int[] getRouteTable() {
//        if (null == sRouteTable) {
//            switch (getCurrentTvSystem()) {
//                case TvCommonManager.TV_SYSTEM_DVBT:
//                case TvCommonManager.TV_SYSTEM_DVBC:
//                case TvCommonManager.TV_SYSTEM_DVBS:
//                case TvCommonManager.TV_SYSTEM_DVBT2:
//                case TvCommonManager.TV_SYSTEM_DVBS2:
//                case TvCommonManager.TV_SYSTEM_DTMB:
//                case TvCommonManager.TV_SYSTEM_ISDB:
//                    TvTypeInfo tvinfo = TvCommonManager.getInstance().getTvInfo();
//                    sRouteTable = Arrays.copyOf(tvinfo.routePath, tvinfo.routePath.length);
//                    break;
//                case TvCommonManager.TV_SYSTEM_ATSC:
//                    sRouteTable = new int[] {
//                            TvChannelManager.DTV_ANTENNA_TYPE_NONE,
//                            TvChannelManager.DTV_ANTENNA_TYPE_AIR,
//                            TvChannelManager.DTV_ANTENNA_TYPE_CABLE
//                    };
//                    break;
//                default:
//                    break;
//            }
//        }
//        return sRouteTable;
//    }
//
////    public static String[] getRouteTableName() {
////        if (null == sRouteTableName) {
////            int tvSystem = getCurrentTvSystem();
////            int[] routeTable = getRouteTable();
////
////            if (0 < routeTable.length) {
////                sRouteTableName = new String[routeTable.length];
////                /* Fill the literal representation of tv route */
////                for (int i = 0; i < sRouteTableName.length; ++i) {
////                    sRouteTableName[i] = tvRouteToString(routeTable[i]);
////                }
////            } else {
////                sRouteTableName = new String[] {
////                        tvRouteToString(-1)
////                };
////            }
////        }
////        return sRouteTableName;
////    }
//
//
////    private static String tvRouteToString(int nTvRoute) {
////        switch (getCurrentTvSystem()) {
////            case TvCommonManager.TV_SYSTEM_DVBT:
////            case TvCommonManager.TV_SYSTEM_DVBC:
////            case TvCommonManager.TV_SYSTEM_DVBS:
////            case TvCommonManager.TV_SYSTEM_DVBT2:
////            case TvCommonManager.TV_SYSTEM_DVBS2:
////            case TvCommonManager.TV_SYSTEM_DTMB:
////                switch (nTvRoute) {
////                    case TvChannelManager.TV_ROUTE_NONE:
////                        return getContext().getResources()
////                                .getString(R.string.str_cha_tv_route_none);
////                    case TvChannelManager.TV_ROUTE_DVBT:
////                        return getContext().getResources()
////                                .getString(R.string.str_cha_tv_route_dvbt);
////                    case TvChannelManager.TV_ROUTE_DVBC:
////                        return getContext().getResources()
////                                .getString(R.string.str_cha_tv_route_dvbc);
////                    case TvChannelManager.TV_ROUTE_DVBS:
////                        return getContext().getResources()
////                                .getString(R.string.str_cha_tv_route_dvbs);
////                    case TvChannelManager.TV_ROUTE_DVBT2:
////                        return getContext().getResources().getString(
////                                R.string.str_cha_tv_route_dvbt2);
////                    case TvChannelManager.TV_ROUTE_DVBS2:
////                        return getContext().getResources().getString(
////                                R.string.str_cha_tv_route_dvbs2);
////                    case TvChannelManager.TV_ROUTE_DTMB:
////                        return getContext().getResources()
////                                .getString(R.string.str_cha_tv_route_dtmb);
////                    default:
////                        return getContext().getResources().getString(
////                                R.string.str_cha_tv_route_unknown);
////                }
////            case TvCommonManager.TV_SYSTEM_ATSC:
////                switch (nTvRoute) {
////                    case TvChannelManager.DTV_ANTENNA_TYPE_NONE:
////                        return getContext().getResources().getString(
////                                R.string.str_cha_antannatype_none);
////                    case TvChannelManager.DTV_ANTENNA_TYPE_AIR:
////                        return getContext().getResources().getString(
////                                R.string.str_cha_antannatype_air);
////                    case TvChannelManager.DTV_ANTENNA_TYPE_CABLE:
////                        return getContext().getResources().getString(
////                                R.string.str_cha_antannatype_cable);
////                    default:
////                        return getContext().getResources().getString(
////                                R.string.str_cha_tv_route_unknown);
////                }
////            case TvCommonManager.TV_SYSTEM_ISDB:
////                return getContext().getResources().getString(R.string.str_cha_tv_route_unknown);
////            default:
////                // Log.d(TAG,
////                // "tvRouteToString():: Do Not Fall into the default case ! Handle Different DTV System Above !!");
////                return getContext().getResources().getString(R.string.str_cha_tv_route_unknown);
////        }
////    }
//
////    public static void setDefaultFocus(ViewGroup viewGroup) {
////        if (null != viewGroup) {
////            View focusedView = (View) viewGroup.getFocusedChild();
////            View view = null;
////            boolean hasFocus = false;
////
////            if (null != focusedView) {
////                for (int index = 0; index < viewGroup.getChildCount(); index++) {
////                    view = viewGroup.getChildAt(index);
////                    if (null != view) {
////                        if (view.getId() == focusedView.getId()) {
////                            hasFocus = true;
////                            break;
////                        }
////                    }
////                }
////            }
////
////            if (false == hasFocus) {
////                for (int index = 0; index < viewGroup.getChildCount(); index++) {
////                    view = viewGroup.getChildAt(index);
////                    if (null != view) {
////                        if ((true == view.isFocusable()) && (View.GONE != view.getVisibility())) {
////                            view.requestFocus();
////                            break;
////                        }
////                    }
////                }
////            }
////        }
////    }
//
//    public static int getATVRealChNum(int chNo) {
//        int num = chNo;
//        if (TvCommonManager.getInstance().isSupportModule(TvCommonManager.MODULE_ATV_PAL_ENABLE)) {
//            /*
//             * In Pal system, ATV channel number saving in db is start from 0
//             * but display number is start from 1 (NTSC system has no this
//             * problem)
//             */
//            num -= 1;
//        } else if (TvCommonManager.getInstance().isSupportModule(
//                TvCommonManager.MODULE_ATV_NTSC_ENABLE)) {
//            if (isISDB()) {
//                num -= 1;
//            }
//        }
//        return num;
//    }
//
//    public static int getATVDisplayChNum(int chNo) {
//        int num = chNo;
//        if (TvCommonManager.getInstance().isSupportModule(TvCommonManager.MODULE_ATV_PAL_ENABLE)) {
//            /*
//             * In Pal system, ATV channel number saving in db is start from 0
//             * but display number is start from 1 (NTSC system has no this
//             * problem)
//             */
//            num += 1;
//        } else if (TvCommonManager.getInstance().isSupportModule(
//                TvCommonManager.MODULE_ATV_NTSC_ENABLE)) {
//            if (isISDB()) {
//                num += 1;
//            }
//        }
//
//        return num;
//    }
//
//    public static boolean isSupportATV() {
//        if ((false == TvCommonManager.getInstance().isSupportModule(
//                TvCommonManager.MODULE_ATV_NTSC_ENABLE))
//                && (false == TvCommonManager.getInstance().isSupportModule(
//                TvCommonManager.MODULE_ATV_PAL_ENABLE))) {
//            return false;
//        }
//        return true;
//    }
//
//    public static int getDefaultInputSource() {
//        int src = TvCommonManager.INPUT_SOURCE_ATV;
//        if (false == isSupportATV()) {
//            src = TvCommonManager.INPUT_SOURCE_DTV;
//        }
//        return src;
//    }
//
//    public static boolean isSupportInputSourceLock() {
//        return TvCommonManager.getInstance().isSupportModule(
//                TvCommonManager.MODULE_INPUT_SOURCE_LOCK);
//    }
//
//    /*
//     * FIXME: By Android Api Guide, getRunningTasks should not be used in our
//     * code's core section. It is only using for debugging. But we do not have a
//     * better method to determinant whether RootActivity is in top or not, So we
//     * used this method temporarily.
//     */
//    public static boolean isTopActivity(Activity activity, String className) {
//        ActivityManager manager = (ActivityManager) activity
//                .getSystemService(Context.ACTIVITY_SERVICE);
//        ComponentName cn = manager.getRunningTasks(1).get(0).topActivity;
//        String topActivityName = cn.getClassName();
//        return topActivityName.equals(className);
//    }
//
//    public static void startSourceInfo(Activity activity) {
//        boolean isSystemLocked = false;
//        boolean isCurrentProgramLocked = false;
//        int curInput = TvCommonManager.getInstance().getCurrentTvInputSource();
//        if (TvCommonManager.getInstance().getCurrentTvSystem() == TvCommonManager.TV_SYSTEM_ATSC) {
//            isSystemLocked = TvAtscChannelManager.getInstance().getCurrentVChipBlockStatus();
//        } else {
//            isSystemLocked = TvParentalControlManager.getInstance().isSystemLock();
//        }
//        Log.d(TAG, "isSystemLocked = " + isSystemLocked);
//        Log.d(TAG, "curInput = " + curInput);
//        if ((TvCommonManager.INPUT_SOURCE_ATV == curInput)
//                || (TvCommonManager.INPUT_SOURCE_DTV == curInput)) {
//            isCurrentProgramLocked = TvChannelManager.getInstance().getCurrentProgramInfo().isLock;
//        }
//        Log.d(TAG, "isCurrentProgramLocked = " + isCurrentProgramLocked);
//        if (!(isSystemLocked && isCurrentProgramLocked)) {
//            /*
//             * when RootActivity is not running, we don't start activity to
//             * interrupt other menu, so we send SIGNAL_LOCK action to source
//             * info for updating content if SourceInfoActivity is alive, its
//             * BoradcastReceiver will handle this event.
//             */
//            if (false == isTopActivity(activity, RootActivity.class.getName())) {
//                activity.sendBroadcast(new Intent(TvIntent.ACTION_SIGNAL_LOCK));
//            } else {
//                SharedPreferences settings = activity.getSharedPreferences(Constant.PREFERENCES_TV_SETTING,
//                        Context.MODE_PRIVATE);
//                final boolean isLocationSelected = settings.getBoolean(Constant.PREFERENCES_IS_LOCATION_SELECTED, false);
//                final boolean isAutoscanLaunched = settings.getBoolean(Constant.PREFERENCES_IS_AUTOSCAN_LAUNCHED, false);
//                Log.d(TAG, "is location selected = " + isLocationSelected);
//                Log.d(TAG, "is autoscan lauched = " + isAutoscanLaunched);
//                if ((true == isLocationSelected) && (true == isAutoscanLaunched)) {
//                    Intent intent = new Intent(TvIntent.ACTION_SOURCEINFO);
//                    activity.startActivity(intent);
//                }
//            }
//        }
//    }
//
//    public static void doChannelChange(Activity activity, int operation, int channelSelectNum) {
//        Log.d(TAG, "doChannelChange(), operation = " + operation);
//        TvChannelManager channelManager = TvChannelManager.getInstance();
//        if (DO_CHANNEL_UP == operation) {
//            channelManager.programUp();
//        } else if (DO_CHANNEL_DOWN == operation) {
//            channelManager.programDown();
//        } else if (DO_CHANNEL_RETURN == operation) {
//            channelManager.returnToPreviousProgram();
//        } else if (DO_CHANNEL_SELECT == operation) {
//            channelManager.selectProgram(channelSelectNum, TvChannelManager.SERVICE_TYPE_DTV);
//        } else {
//            DebugUtils.LogE(TAG, "unhandled case!");
//            return;
//        }
//        if (TvCommonManager.getInstance().getCurrentTvSystem() != TvCommonManager.TV_SYSTEM_ATSC) {
//            startSourceInfo(activity);
//        }
//    }
//
//    public static boolean isForegroundRecording() {
//        boolean isForegroundRecord = false;
//        /* Always time shift recording will auto stop by tvsystem. */
//        final TvPvrManager pvr = TvPvrManager.getInstance();
//        if ((true == pvr.isRecording()) && (false == pvr.isAlwaysTimeShiftRecording())) {
//            isForegroundRecord = true;
//        }
//        return isForegroundRecord;
//    }
//
//    private static void stopPvr(final Activity activity, boolean isForegroundRecording,
//                                boolean isPlaybacking) {
//        if (true == PVRActivity.isPVRActivityActive) {
//            Intent intent = new Intent(TvIntent.ACTION_PVR_ACTIVITY);
//            if (isForegroundRecording) {
//                intent.putExtra(Constant.PVR_CREATE_MODE, Constant.PVR_RECORD_STOP);
//            } else {
//                intent.putExtra(Constant.PVR_CREATE_MODE, Constant.PVR_PLAYBACK_STOP);
//            }
//            if (intent.resolveActivity(activity.getPackageManager()) != null) {
//                activity.startActivity(intent);
//            }
//        } else {
//            if (isForegroundRecording) {
//                TvPvrManager.getInstance().stopRecord();
//            }
//            if (isPlaybacking) {
//                TvPvrManager.getInstance().stopPlayback();
//            }
//        }
//        /* Return util exit PVRActivity */
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    while (true == PVRActivity.isPVRActivityActive) {
//                        Thread.sleep(50);
//                    }
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//        return;
//    }
//
//    public static void channelChangeDialog(final Activity activity, final int operation,
//                                           final int channelSelectNum) {
////        final boolean isForegroundRecording = isForegroundRecording();
////        final boolean isPlaybacking = TvPvrManager.getInstance().isPlaybacking();
////        if ((true == isForegroundRecording) || (true == isPlaybacking)) {
////            AlertDialog.Builder build = new AlertDialog.Builder(activity);
////            if (isForegroundRecording) {
////                build.setTitle(R.string.str_stop_record_dialog_title);
////                if (isPlaybacking) {
////                    build.setMessage(R.string.str_stop_record_playback_dialog_message);
////                } else {
////                    build.setMessage(R.string.str_stop_record_dialog_message);
////                }
////            } else {
////                build.setTitle(R.string.str_stop_playback_dialog_title);
////                build.setMessage(R.string.str_stop_playback_dialog_message);
////            }
////            build.setPositiveButton(R.string.str_stop_record_dialog_stop,
////                    new DialogInterface.OnClickListener() {
////
////                        @Override
////                        public void onClick(DialogInterface dialog, int which) {
////                            stopPvr(activity, isForegroundRecording, isPlaybacking);
////                            doChannelChange(activity, operation, channelSelectNum);
////                        }
////                    });
////            build.setNegativeButton(R.string.str_stop_record_dialog_cancel,
////                    new DialogInterface.OnClickListener() {
////
////                        @Override
////                        public void onClick(DialogInterface dialog, int which) {
////
////                        }
////                    });
////            build.create().show();
////            return;
////        }
//        doChannelChange(activity, operation, channelSelectNum);
//    }
//
//    public static void channelUp(Activity activity) {
//        channelChangeDialog(activity, DO_CHANNEL_UP, 0);
//    }
//
//    public static void channelDown(Activity activity) {
//        channelChangeDialog(activity, DO_CHANNEL_DOWN, 0);
//    }
//
//    public static void channelReturn(Activity activity) {
//        channelChangeDialog(activity, DO_CHANNEL_RETURN, 0);
//    }
//
//    public static void channelSelect(Activity activity, int channelSelectNum) {
//        channelChangeDialog(activity, DO_CHANNEL_SELECT, channelSelectNum);
//    }
//
//    public static void changeTvTimeZone(Context context, String timezoneChangeString) {
//        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        alarm.setTimeZone(timezoneChangeString);
//        Toast.makeText(context,
//                getContext().getResources().getString(R.string.str_time_change_timezone),
//                Toast.LENGTH_SHORT).show();
//    }
//
//    public static boolean scrollPosition(TextView tv, int direction) {
//        int lineCount = tv.getLineCount();
//        int linesOfOnePage = tv.getHeight() / tv.getLineHeight();
//        if (lineCount > linesOfOnePage) {
//            Layout layout = tv.getLayout();
//            int currentLine = layout.getLineForVertical(tv.getScrollY());
//            int newLine = 0;
//
//            if (direction == SCROLL_DIRECTION_UP) {
//                /* scroll one page up */
//                newLine = currentLine - linesOfOnePage;
//                if (0 > newLine) {
//                    newLine = 0;
//                }
//            } else if (direction == SCROLL_DIRECTION_DOWN) {
//                /* scroll one page down */
//                newLine = currentLine + linesOfOnePage;
//                if (newLine >= lineCount) {
//                    newLine = currentLine;
//                }
//            }
//
//            tv.scrollTo(0, layout.getLineTop(newLine));
//            return true;
//        }
//
//        return false;
//    }
//
//    public static void showLocationCodeInputDialog(Activity activity) {
//        class LocationCodeTextWatcher implements TextWatcher {
//            private AlertDialog alertDialog;
//
//            private final int MAX_TEXT_LEN = 5;
//
//            public LocationCodeTextWatcher(AlertDialog d) {
//                alertDialog = d;
//            }
//
//            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
//            }
//
//            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
//                int count = arg0.length();
//                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(
//                        (count >= MAX_TEXT_LEN) ? true : false);
//            }
//
//            public void afterTextChanged(Editable arg0) {
//            }
//        }
//
//        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//        builder.setTitle(activity.getResources().getString(R.string.str_ews_location_code));
//        final View view = activity.getLayoutInflater().inflate(
//                R.layout.location_code_input_dialog_5_digits, null);
//        EditText editText = (EditText) view.findViewById(R.id.input_dialog_edittext);
//        builder.setView(view);
//        builder.setPositiveButton(
//                activity.getResources().getString(R.string.str_ews_location_code_save),
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        EditText editText = (EditText) view
//                                .findViewById(R.id.input_dialog_edittext);
//                        TvDvbChannelManager.getInstance().setUserLocationCode(
//                                Integer.valueOf(editText.getText().toString()));
//                        dialog.cancel();
//                    }
//                });
//        builder.setNegativeButton(
//                activity.getResources().getString(R.string.str_ews_location_code_cancel),
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        dialog.cancel();
//                    }
//                });
//
//        AlertDialog alertDialog = builder.create();
//        editText.addTextChangedListener(new LocationCodeTextWatcher(alertDialog));
//        alertDialog.show();
//        String str = String.format("%05d", TvDvbChannelManager.getInstance().getUserLocationCode());
//        editText.setText(str);
//
//        editText.setOnKeyListener(new OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
//                    EditText editText = (EditText) v.findViewById(R.id.input_dialog_edittext);
//                    if (editText.getText().toString().length() > 0) {
//                        editText.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
//                                KeyEvent.KEYCODE_DEL, 0));
//                        editText.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_UP,
//                                KeyEvent.KEYCODE_DEL, 0));
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });
//    }
//
//    public static String getParentalGuideAgeString(int ageValue, int country) {
//        if (0 >= ageValue) {
//            return "";
//        }
//
//        int ageStringResId = R.array.guidance_list_default_string;
//        String[] ageString = null;
//        if (TvCountry.AUSTRALIA == country) {
//            if (AUSTRALIAN_PARENTAL_VALUE_BLOCK_ALL_VALUE == ageValue) {
//                /*
//                 * The displaying string of rating Block-All is the empty
//                 * string.
//                 */
//                return (new String(""));
//            }
//            ageStringResId = R.array.guidance_list_australian_string;
//        } else if (TvCountry.SINGAPORE == country) {
//            ageStringResId = R.array.guidance_list_singapore_string;
//        } else {
//            ageStringResId = R.array.guidance_list_default_string;
//        }
//        ageString = getContext().getResources().getStringArray(ageStringResId);
//
//        return ageString[getParentalGuideIndex(ageValue, country)];
//    }
//
//    public static int getParentalGuideIndex(int ageValue, int country) {
//        int[] level = null;
//        if (TvCountry.AUSTRALIA == country) {
//            level = sParentalValueAustralian;
//        } else if (TvCountry.SINGAPORE == country) {
//            level = sParentalValueSingapore;
//        } else {
//            level = sParentalValueDefault;
//        }
//
//        int ageIndex = 0;
//        for (int i = (level.length - 1); i >= 0; i--) {
//            if (ageValue >= level[i]) {
//                ageIndex = i;
//                break;
//            }
//        }
//        return ageIndex;
//    }
//
//    public static int getParentalGuideValue(int ageIndex, int country) {
//        if (TvCountry.AUSTRALIA == country) {
//            return sParentalValueAustralian[ageIndex];
//        } else if (TvCountry.SINGAPORE == country) {
//            return sParentalValueSingapore[ageIndex];
//        } else {
//            return sParentalValueDefault[ageIndex];
//        }
//    }
//
//    public static String getAudioTypeString(int audioType, int aacLevel) {
//        int resId = 0;
//        switch (audioType) {
//            case TvAudioManager.AUDIO_TYPE_MPEG:
//                resId = R.string.audio_type_mpeg;
//                break;
//            case TvAudioManager.AUDIO_TYPE_Dolby_D:
//                resId = R.string.audio_type_dolby_d;
//                break;
//            case TvAudioManager.AUDIO_TYPE_AAC:
//                resId = R.string.audio_type_aac;
//                switch (aacLevel) {
//                    case TvAudioManager.AAC_LEVEL1:
//                        resId = R.string.aac_level1;
//                        break;
//                    case TvAudioManager.AAC_LEVEL2:
//                        resId = R.string.aac_level2;
//                        break;
//                    case TvAudioManager.AAC_LEVEL4:
//                        resId = R.string.aac_level4;
//                        break;
//                    case TvAudioManager.AAC_LEVEL5:
//                        resId = R.string.aac_level5;
//                        break;
//                    case TvAudioManager.HE_AAC_LEVEL2:
//                        resId = R.string.he_aac_level2;
//                        break;
//                    case TvAudioManager.HE_AAC_LEVEL3:
//                        resId = R.string.he_aac_level3;
//                        break;
//                    case TvAudioManager.HE_AAC_LEVEL4:
//                        resId = R.string.he_aac_level4;
//                        break;
//                    case TvAudioManager.HE_AAC_LEVEL5:
//                        resId = R.string.he_aac_level5;
//                        break;
//                    default:
//                        if (TvCommonManager.TV_SYSTEM_ISDB == getCurrentTvSystem()) {
//                            switch (aacLevel) {
//                                case TvAudioManager.AAC_LEVEL1_BRAZIL:
//                                    resId = R.string.aac_level1;
//                                    break;
//                                case TvAudioManager.AAC_LEVEL2_BRAZIL:
//                                    resId = R.string.aac_level2;
//                                    break;
//                                case TvAudioManager.AAC_LEVEL4_BRAZIL:
//                                    resId = R.string.aac_level4;
//                                    break;
//                                case TvAudioManager.AAC_LEVEL5_BRAZIL:
//                                    resId = R.string.aac_level5;
//                                    break;
//                                case TvAudioManager.HE_AAC_LEVEL2_BRAZIL:
//                                    resId = R.string.he_aac_level2;
//                                    break;
//                                case TvAudioManager.HE_AAC_LEVEL3_BRAZIL:
//                                    resId = R.string.he_aac_level3;
//                                    break;
//                                case TvAudioManager.HE_AAC_LEVEL4_BRAZIL:
//                                    resId = R.string.he_aac_level4;
//                                    break;
//                                case TvAudioManager.HE_AAC_LEVEL5_BRAZIL:
//                                    resId = R.string.he_aac_level5;
//                                    break;
//                            }
//                        }
//                }
//                break;
//            case TvAudioManager.AUDIO_TYPE_AC3P:
//                resId = R.string.audio_type_ac3p;
//                break;
//            case TvAudioManager.AUDIO_TYPE_DRA1:
//                resId = R.string.audio_type_dra1;
//                break;
//            default:
//                return new String("");
//        }
//
//        return getContext().getResources().getString(resId);
//    }
//
//    /**
//     * Ths is a utility function to short the string and make it readable.
//     */
//    public static String getStrLimited(final String inpStr, final int maxLength) {
//        if (0 >= maxLength) {
//            return new String("");
//        }
//        if (inpStr.length() < maxLength) {
//            return inpStr;
//        } else {
//            final String space = " ";
//            final String[] wordList = inpStr.split(space);
//            String retString = "";
//            for (int i = 0; i < wordList.length; i++) {
//                final String word = wordList[i];
//                if ((retString.length() + word.length()) > maxLength) {
//                    break;
//                }
//                retString += word;
//                retString += space;
//            }
//            if (retString.isEmpty()) {
//                return inpStr.substring(0, (maxLength - 1));
//            } else {
//                return new String(retString.trim());
//            }
//        }
//    }
//
//    public static String ttsGetLinearLayoutString(final LinearLayout ll) {
//        String str = "";
//        boolean first = true;
//        final int count = ll.getChildCount();
//        for (int i = 0; i < count; i++) {
//            final View textView = ll.getChildAt(i);
//            if (textView instanceof TextView) {
//                if (false == first) {
//                    str += ", ";
//                }
//                str += ((TextView) textView).getText().toString();
//                first = false;
//            }
//        }
//        return new String(str);
//    }
//
//    public static void ttsSepakLinearLayout(final LinearLayout ll) {
//        String str = ttsGetLinearLayoutString(ll);
//        if (!str.isEmpty()) {
//            TvCommonManager.getInstance().speakTtsDelayed(
//                    str
//                    , TvCommonManager.TTS_QUEUE_FLUSH
//                    , TvCommonManager.TTS_SPEAK_PRIORITY_NORMAL
//                    , TvCommonManager.TTS_DELAY_TIME_100MS);
//        }
//    }
//
//    public static void initLittleDownCounter() {
//        LittleDownTimer.getInstance().start();
//        int value = TvCommonManager.getInstance().getOsdTimeoutInSecond();
//        if (value < 1) {
//            value = 5;
//        }
//        if (value > 30) {
//            LittleDownTimer.stopMenu();
//        } else {
//            LittleDownTimer.setMenuStatus(value);
//        }
//    }
//
//    public static boolean isSiganlLocked() {
//        int curInputSource = TvCommonManager.getInstance().getCurrentTvInputSource();
//        int signalStatus = TvChannelManager.getInstance().getSignalStatus(curInputSource);
//        if (TvChannelManager.TVPLAYER_SIGNAL_LOCK == signalStatus) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    public static String getHdmiDispStr(int inputSource) {
//        final int FUNCTION_DISABLED = 0;
//        int[] sourceList = TvCommonManager.getInstance().getSourceList();
//        int hdmiPortNum = 0;
//        int typeCPortNum = 0;
//        String srcName = "";
//        boolean isTypeC = false;
//        if ((TvCommonManager.INPUT_SOURCE_HDMI <= inputSource)
//                && (TvCommonManager.INPUT_SOURCE_HDMI4 >= inputSource)) {
//            for (int i = TvCommonManager.INPUT_SOURCE_HDMI; i < TvCommonManager.INPUT_SOURCE_HDMI_MAX; i++) {
//                if (FUNCTION_DISABLED != sourceList[i]) {
//                    hdmiPortNum++;
//                }
//            }
//            boolean[] hdmiPortStatus = TvCommonManager.getInstance().getHdmiTypeCPort();
//            if (null != hdmiPortStatus) {
//                for (int i = 0; i < hdmiPortStatus.length; i++) {
//                    if (true == hdmiPortStatus[i]) {
//                        typeCPortNum++;
//                    }
//                }
//            }
//            hdmiPortNum = hdmiPortNum - typeCPortNum;
//            final int idx = inputSource - TvCommonManager.INPUT_SOURCE_HDMI;
//            if ((null != hdmiPortStatus) && (hdmiPortStatus[idx])) {
//                srcName = "TYPE-C";
//                if (1 < typeCPortNum) {
//                    int count = 0;
//                    for (int i = 0; i < hdmiPortStatus.length; i++) {
//                        if (true == hdmiPortStatus[i]) {
//                            count++;
//                        }
//                        if (idx == i) {
//                            break;
//                        }
//                    }
//                    srcName = "TYPE-C " + count;
//                }
//                isTypeC = true;
//            }
//            if (false == isTypeC) {
//                srcName = "HDMI";
//                if (1 < hdmiPortNum) {
//                    int count = 0;
//                    for (int i = TvCommonManager.INPUT_SOURCE_HDMI; i < TvCommonManager.INPUT_SOURCE_HDMI_MAX; i++) {
//                        if (FUNCTION_DISABLED != sourceList[i]) {
//                            if (true == hdmiPortStatus[i - TvCommonManager.INPUT_SOURCE_HDMI]) {
//                                continue;
//                            }
//                            count++;
//                            if (inputSource == i) {
//                                break;
//                            }
//                        }
//                    }
//                    srcName = "HDMI" + count;
//                }
//            }
//        }
//        /*
//         * HW: HDMI, HDMI, HDMI, HDMI
//         * Displaying: HDMI1, HDMI2, HDMI3, HDMI4
//         *
//         * HW: HDMI, TYPE C, HDMI, HDMI
//         * Displaying HDMI1, TYPE-C, HDMI2, HDMI3
//         *
//         * HW: HDMI, TYPE C, HDMI, TYPE C
//         * Displaying: HDMI1, TYPE-C 1, HDMI2, TYPE-C 2
//         *
//         * HW: TYPE C, HDMI, HDMI, HDMI
//         * Displaying: TYPE-C, HDMI1, HDMI2, HDMI3
//         */
//        return srcName;
//    }
//}
