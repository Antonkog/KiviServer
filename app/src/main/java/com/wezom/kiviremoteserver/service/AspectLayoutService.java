package com.wezom.kiviremoteserver.service;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mstar.android.tv.TvCommonManager;
import com.mstar.android.tv.TvPictureManager;
import com.mstar.android.tvapi.common.vo.ColorTemperatureExData;
import com.wezom.kiviremoteserver.R;
import com.wezom.kiviremoteserver.service.aspect.Alarm;
import com.wezom.kiviremoteserver.service.aspect.HDRValues;
import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper;
import com.wezom.kiviremoteserver.ui.views.KeyListener;
import com.wezom.kiviremoteserver.ui.views.LRTextSwitcher;
import com.wezom.kiviremoteserver.ui.views.NumberDialing;
import com.wezom.kiviremoteserver.ui.views.ScreenProgress;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import wezom.kiviremoteserver.environment.bridge.BridgePicture;
import wezom.kiviremoteserver.environment.bridge.driver_set.PictureMode;
import wezom.kiviremoteserver.environment.bridge.driver_set.Ratio;
import wezom.kiviremoteserver.environment.bridge.driver_set.TemperatureValues;

//com.wezom.kiviremoteserver.mstar.java.launcher.kivi.com.kivilauncher.environment.bridge.driver_set.
//import wezom.kiviremoteserver.environment.bridge.driver_set.PictureMode;
//import wezom.kiviremoteserver.environment.bridge.driver_set.Ratio;
//Ratio

public class AspectLayoutService extends Service implements View.OnKeyListener {

    private int generalType = BridgePicture.LAYER_TYPE;//WindowManager.LayoutParams.TYPE_TOAST;
    private WindowManager wmgr;
    private RelativeLayout generalView;
    private LinearLayout headerContainer;
    private LinearLayout bodyContainer;
    private LayoutInflater layoutInflater;
    private HorizontalScrollView bodyInputs;
    private TextView description;
    private NumberDialing bodyChannel;
    private View channelHeader;
    private TvPictureManager pictureManager;

    private List<PictureMode> modes = Arrays.asList(PictureMode.PICTURE_MODE_NORMAL,
            PictureMode.PICTURE_MODE_SOFT,
            PictureMode.PICTURE_MODE_USER,
            PictureMode.PICTURE_MODE_AUTO,
            PictureMode.PICTURE_MODE_VIVID);
    //      PictureMode.PICTURE_MODE_ECONOMY);
    private List<Ratio> ratios = Arrays.asList(
            Ratio.VIDEO_ARC_DEFAULT,
            Ratio.VIDEO_ARC_16x9,
            Ratio.VIDEO_ARC_4x3,
            Ratio.VIDEO_ARC_AUTO);


    private boolean sleepFocused;
    private boolean isUHD = false;
    private boolean isHaveChannel = false;

    private List<Integer> shutDownTimers = Arrays.asList(-1, 10, 20, 30, 60, 120);
    private int alarmPosition = 0;
    private int minutesLeft;
    private long slipIn = 0;

    private int contrast = 0;
    private int brightness = 0;
    private int sharpness = 0;
    private int saturation = 0;
    private int backlight = 0;
    private int color_r = 0;
    private int color_g = 0;
    private int color_b = 0;


    public static volatile long lastUpdate;
    private Handler timer = new Handler();
    private Runnable updateSleepTime = new Runnable() {
        @Override
        public void run() {
            if (sleepFocused) {
                updateSleepText();
            }

            timer.postDelayed(updateSleepTime, 1000);
            if (System.currentTimeMillis() - 8000 > lastUpdate) {
                stopSelf();
            }
        }
    };

    private void updateSleepText() {
        String timeStr = "";
        long leftTime = (slipIn - System.currentTimeMillis()) / 1000;
        int timeToSleep = -1;
        if (leftTime > 0) {
            timeToSleep = (int) leftTime;
        }
        if (timeToSleep > 0) {
            if (timeToSleep >= 3600) {
                timeStr += timeToSleep / 3600 + getString(R.string.double_dot);
            }
            if (timeToSleep >= 60) {

                timeStr += String.format("%02d", timeToSleep % 3600 / 60) + getString(R.string.double_dot);
            }
            timeStr += String.format("%02d", timeToSleep % 60);
            description.setText(String.format(getResources().getString(R.string.sleep_desc), timeStr));
        } else {
            description.setText(R.string.timer_off);

        }
    }

    //android.widget.LinearLayout{e7580bb VFE...C.. .F...... 444,0-554,98 #7f090128 app:id/root}
    @Override
    public void onCreate() {
        super.onCreate();
        pictureManager = TvPictureManager.getInstance();
        String MODEL = Build.MODEL;
        isUHD = MODEL != null && (MODEL.contains("U") || MODEL.contains("u"));
        // Log.e("MODEL", "name " + MODEL);
        // new Handler().postDelayed(() -> {
        lastUpdate = System.currentTimeMillis();
        createLayout(getBaseContext());
        //}, 5000);
        timer.post(updateSleepTime);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

//    boolean isHaveChannel = false;

    public void createLayout(Context context) {
        //Log.e("create", "create");
        pictureManager = TvPictureManager.getInstance();
        layoutInflater = (LayoutInflater)
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        wmgr = (WindowManager) context.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        generalView = (RelativeLayout) View.inflate(context, R.layout.layout_aspect, null);
        generalView.setVisibility(View.VISIBLE);
        final WindowManager.LayoutParams param = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                generalType,//TYPE_SYSTEM_ALERT
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT);
        wmgr.addView(generalView, param);

        headerContainer = generalView.findViewById(R.id.header_container);
        bodyContainer = generalView.findViewById(R.id.body_container);
        description = generalView.findViewById(R.id.description);
        int currewnt = TvCommonManager.getInstance().getCurrentTvInputSource();
        if (currewnt == 28 || currewnt == 1) {
            isHaveChannel = true;
        }
        addOptimization(headerContainer);
        addSeparator(headerContainer);
        addPictureSetting(headerContainer);
        addSeparator(headerContainer);
//        addTemperatureSetting(headerContainer);
//        addSeparator(headerContainer);
        addInputs(headerContainer);
        addSeparator(headerContainer);
        addSleep(headerContainer);
        addSeparator(headerContainer);
        addPictureMode(headerContainer);
        addSeparator(headerContainer);
        addRatio(headerContainer);

        if (isHaveChannel) {
            addSeparator(headerContainer);
            addChannelSelector(headerContainer);
        }
//        handler.postDelayed(runnable, 1000);
        // generalView.requestFocus();
//        generalView.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (keyCode == 4) {
//                    stopSelf();
//                }
//                return false;
//            }
//        });
    }


    private void addSeparator(LinearLayout headerContainer) {
        View view = new View(headerContainer.getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(2, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.bottomMargin = 10;
        layoutParams.topMargin = 10;
        view.setLayoutParams(layoutParams);
        view.setBackgroundResource(R.drawable.vertical_center_white);
        headerContainer.addView(view);
    }


    @Override
    public void onDestroy() {
        if (timer != null) timer.removeCallbacks(updateSleepTime);
        if (wmgr != null && generalView != null) wmgr.removeView(generalView);
        super.onDestroy();
    }


    private void addPictureSetting(LinearLayout generalView) {
        View view = layoutInflater.inflate(R.layout.aspect_header_img, generalView, false);
        ((ImageView) view.findViewById(R.id.image)).setImageResource(R.drawable.lb_ic_pip);

        LinearLayout bodyPictureSettings = (LinearLayout) layoutInflater.inflate(R.layout.picture_settings, bodyContainer, false);
        view.setOnFocusChangeListener((v, hasFocus) -> {
//
            if (hasFocus) {
                bodyContainer.removeAllViews();
                bodyContainer.addView(bodyPictureSettings);
                description.setText(R.string.picture_des);

            }
        });
        pictureDetailSettings(bodyPictureSettings);
        List<View> list = new ArrayList<>();
        KeyListener listener = new KeyListener(list, view);
        for (int i = bodyPictureSettings.getChildCount() - 1; i >= 0; i--) {
            list.add(bodyPictureSettings.getChildAt(i));
            bodyPictureSettings.getChildAt(i).setOnKeyListener(listener);
        }
        actions.put(view, new KeyActions()
                .addAction(KeyEvent.KEYCODE_DPAD_UP, () -> {
                    if (isSafe()) {
                        list.get(0).requestFocus();
                        list.get(0).requestFocus();
                    }
                }).setAction(KeyEvent.ACTION_UP)
        );
        generalView.addView(view);
        view.setOnKeyListener(this);
    }


    private void addTemperatureSetting(LinearLayout generalView) {

        View view = layoutInflater.inflate(R.layout.aspect_header_img, generalView, false);
        ((ImageView) view.findViewById(R.id.image)).setImageResource(R.drawable.ic_speed_24dp);

        LinearLayout bodyPictureSettings = (LinearLayout) layoutInflater.inflate(R.layout.picture_settings, bodyContainer, false);
        view.setOnFocusChangeListener((v, hasFocus) -> {
//
            if (hasFocus) {
                bodyContainer.removeAllViews();
                bodyContainer.addView(bodyPictureSettings);
                description.setText(R.string.temperature_des);
            }
        });


        ColorTemperatureExData colorTemperatureExData = TvPictureManager
                .getInstance().getColorTempratureEx();
        color_r = colorTemperatureExData.redGain;
        color_g = colorTemperatureExData.greenGain;
        color_b = colorTemperatureExData.blueGain;


        pdsColorTemperature(bodyPictureSettings);//
        psdRed(bodyPictureSettings);//??
        psdBlue(bodyPictureSettings);//??
        psdGreen(bodyPictureSettings);//??
        psdHDR(bodyPictureSettings);//??


        generalView.addView(view);
        view.setOnKeyListener(this);

    }

    private void pictureDetailSettings(LinearLayout body) {
        int mPictureMode = TvPictureManager.getInstance().getPictureMode();
        int inputSrcType = TvCommonManager.getInstance()
                .getCurrentTvInputSource();
        Cursor cursor = getApplicationContext()
                .getContentResolver()
                .query(Uri.parse("content://mstar.tv.usersetting/picmode_setting/inputsrc/"
                                + inputSrcType + "/picmode/" + mPictureMode), null,
                        null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            contrast = cursor.getInt(cursor
                    .getColumnIndex("u8Contrast"));
            brightness = cursor.getInt(cursor
                    .getColumnIndex("u8Brightness"));
            sharpness = cursor.getInt(cursor
                    .getColumnIndex("u8Sharpness"));
            saturation = cursor.getInt(cursor
                    .getColumnIndex("u8Saturation"));
            backlight = cursor.getInt(cursor
                    .getColumnIndex("u8Backlight"));
        }
        cursor.close();


        // psdBacklight(body);//+
        psdBrightness(body);//+
        psdContrast(body);//+
        psdSaturation(body);//+
        psdSharpness(body);//+
        psdHDR(body);//+
        psdTemperature(body);//+


    }

    private void psdTemperature(LinearLayout body) {
        TemperatureValues current = TemperatureValues.getByID(TvPictureManager.getInstance().getColorTempratureIdx());
        LRTextSwitcher lrTextSwitcher = new LRTextSwitcher(this);
        lrTextSwitcher.setUpValues(TemperatureValues.COLOR_TEMP_COOL,
                TemperatureValues.COLOR_TEMP_NATURE,
                TemperatureValues.COLOR_TEMP_WARM);
        lrTextSwitcher.setOnKeyListener(this);
        if (current != null)
            lrTextSwitcher.setValue(current);
        lrTextSwitcher.setLable(R.string.temperature_des);
        lrTextSwitcher.setIcon(R.drawable.color_temper_focus);
        lrTextSwitcher.setProgressListener(progress -> TvPictureManager.getInstance().setColorTempratureIdx(progress));
        body.addView(lrTextSwitcher);


    }

    private void psdHDR(LinearLayout body) {
        LRTextSwitcher lrTextSwitcher = new LRTextSwitcher(this);
        HDRValues current = HDRValues.getByID(TvPictureManager.getInstance().getHdrAttributes(TvPictureManager.HDR_OPEN_ATTRIBUTES,
                TvPictureManager.VIDEO_MAIN_WINDOW).level);
        lrTextSwitcher.setUpValues(HDRValues.HDR_OPEN_LEVEL_AUTO,
                HDRValues.HDR_OPEN_LEVEL_LOW,
                HDRValues.HDR_OPEN_LEVEL_MIDDLE,
                HDRValues.HDR_OPEN_LEVEL_HIGH,
                HDRValues.HDR_OPEN_LEVEL_OFF);
        lrTextSwitcher.setOnKeyListener(this);
        if (current != null)
            lrTextSwitcher.setValue(current);
        lrTextSwitcher.setLable(R.string.hdr);
        lrTextSwitcher.setIcon(R.drawable.ic_hdr_strong_black_24dp);
        lrTextSwitcher.setProgressListener(progress -> {
            TvPictureManager.getInstance().setHdrAttributes(TvPictureManager.HDR_OPEN_ATTRIBUTES,
                    TvPictureManager.VIDEO_MAIN_WINDOW, progress);
        });
        body.addView(lrTextSwitcher);

    }

    private void psdGreen(LinearLayout body) {
        ScreenProgress screenProgress = new ScreenProgress(this);
        screenProgress.setOnKeyListener(this);
        screenProgress.setProgress(color_g);
        screenProgress.setLable(R.string.color_g);
        screenProgress.setIcon(R.drawable.ic_color_lens_black_24dp);
        screenProgress.setProgressListener(progress -> TvPictureManager.getInstance().setVideoItem(
                TvPictureManager.PICTURE_SHARPNESS, progress));
        body.addView(screenProgress);
    }

    private void psdBlue(LinearLayout body) {
        ScreenProgress screenProgress = new ScreenProgress(this);
        screenProgress.setOnKeyListener(this);
        screenProgress.setProgress(color_b);
        screenProgress.setLable(R.string.color_b);
        screenProgress.setIcon(R.drawable.ic_color_lens_black_24dp);
        screenProgress.setProgressListener(progress -> TvPictureManager.getInstance().setVideoItem(
                TvPictureManager.PICTURE_SHARPNESS, progress));
        body.addView(screenProgress);
    }

    private void psdRed(LinearLayout body) {
        ScreenProgress screenProgress = new ScreenProgress(this);
        screenProgress.setOnKeyListener(this);
        screenProgress.setProgress(color_r);
        screenProgress.setLable(R.string.color_r);
        screenProgress.setIcon(R.drawable.ic_color_lens_black_24dp);
        screenProgress.setProgressListener(progress -> TvPictureManager.getInstance().setVideoItem(
                TvPictureManager.PICTURE_SHARPNESS, progress));
        body.addView(screenProgress);
    }

    private void psdSharpness(LinearLayout body) {
        ScreenProgress screenProgress = new ScreenProgress(this);
        screenProgress.setOnKeyListener(this);
        screenProgress.setProgress(sharpness);
        screenProgress.setLable(R.string.sharpness);
        screenProgress.setIcon(R.drawable.sharpness_focus);
        screenProgress.setProgressListener(progress -> TvPictureManager.getInstance().setVideoItem(
                TvPictureManager.PICTURE_SHARPNESS, progress));
        body.addView(screenProgress);
    }

    private void psdSaturation(LinearLayout body) {
        ScreenProgress screenProgress = new ScreenProgress(this);
        screenProgress.setOnKeyListener(this);
        screenProgress.setProgress(saturation);
        screenProgress.setLable(R.string.saturation);
        screenProgress.setIcon(R.drawable.saturation_focus);
        screenProgress.setProgressListener(progress -> TvPictureManager.getInstance().setVideoItem(
                TvPictureManager.PICTURE_SATURATION, progress));
        body.addView(screenProgress);
    }

    private void psdContrast(LinearLayout body) {
        ScreenProgress screenProgress = new ScreenProgress(this);
        screenProgress.setOnKeyListener(this);
        screenProgress.setProgress(contrast);
        screenProgress.setLable(R.string.contrast);
        screenProgress.setIcon(R.drawable.contrast_focus);
        screenProgress.setProgressListener(progress -> TvPictureManager.getInstance().setVideoItem(
                TvPictureManager.PICTURE_CONTRAST, progress));
        body.addView(screenProgress);
    }

    private void psdBrightness(LinearLayout body) {
        ScreenProgress screenProgress = new ScreenProgress(this);
        screenProgress.setOnKeyListener(this);
        screenProgress.setProgress(brightness);
        screenProgress.setLable(R.string.brightness);
        screenProgress.setIcon(R.drawable.bright_focus);
        screenProgress.setProgressListener(progress -> TvPictureManager.getInstance().setVideoItem(
                TvPictureManager.PICTURE_BRIGHTNESS, progress));
        body.addView(screenProgress);
    }

    private void psdBacklight(LinearLayout body) {
        ScreenProgress screenProgress = new ScreenProgress(this);
        screenProgress.setOnKeyListener(this);
        screenProgress.setProgress(backlight);
        screenProgress.setLable(R.string.backlight);
        screenProgress.setIcon(R.drawable.backlight_focus);
        screenProgress.setProgressListener(progress -> TvPictureManager.getInstance().setVideoItem(
                TvPictureManager.PICTURE_BACKLIGHT, progress));
        body.addView(screenProgress);

    }

    private void pdsColorTemperature(LinearLayout body) {
        //TvPictureManager.getInstance().setColorTempratureEx();
    }


    private void addPictureMode(LinearLayout generalView) {
        View view = layoutInflater.inflate(R.layout.aspect_header, generalView, false);
        PictureMode currentMode = PictureMode.getByID(pictureManager.getPictureMode());
        if (currentMode == null) {
            currentMode = PictureMode.PICTURE_MODE_NORMAL;
        }


        ((TextView) view.findViewById(R.id.text)).setText(currentMode.getString());
        generalView.addView(view);
        view.setOnFocusChangeListener((v, hasFocus) -> {
            bodyContainer.removeAllViews();
            if (hasFocus) {
                description.setText(R.string.mode_desc);
            }
        });
        view.setOnKeyListener(this);

        actions.put(view, new KeyActions()
                .addAction(KeyEvent.KEYCODE_DPAD_UP, () -> {
                    if (isSafe()) {
                        PictureMode current = PictureMode.getByID(pictureManager.getPictureMode());
                        if (current == null) {
                            current = PictureMode.PICTURE_MODE_NORMAL;
                        }
                        int position = modes.indexOf(current);
                        int newPosition = 0;
                        if (position < modes.size() - 1) {
                            newPosition = position + 1;
                        }
                        current = modes.get(newPosition);
                        pictureManager.setPictureMode(current.getId());
                        ((TextView) view.findViewById(R.id.text)).setText(current.getString());
                    }
                })
                .addAction(KeyEvent.KEYCODE_DPAD_DOWN, () -> {
                    if (isSafe()) {
                        PictureMode current = PictureMode.getByID(pictureManager.getPictureMode());
                        if (current == null) {
                            current = PictureMode.PICTURE_MODE_NORMAL;
                        }
                        int position = modes.indexOf(current);
                        int newPosition = modes.size() - 1;
                        if (position > 0) {
                            newPosition = position - 1;
                        }
                        current = modes.get(newPosition);
                        pictureManager.setPictureMode(current.getId());
                        ((TextView) view.findViewById(R.id.text)).setText(current.getString());
                    }
                })
        );
    }


    private void addSleep(LinearLayout generalView) {
        View view = layoutInflater.inflate(R.layout.aspect_header, generalView, false);

        TextView textView = ((TextView) view.findViewById(R.id.text));

        generalView.addView(view);
        view.setOnFocusChangeListener((v, hasFocus) -> {
            bodyContainer.removeAllViews();
            if (hasFocus) {
                updateSleepText();
            }
            sleepFocused = hasFocus;
//            } else {
//
//            }

        });
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(AspectLayoutService.this, Alarm.class);
        PendingIntent pi = PendingIntent.getService(AspectLayoutService.this, 0, intent, 0);
        slipIn = PreferenceManager.getDefaultSharedPreferences(this).getLong("shutDownTime", -1);
        String text = "";
        if (slipIn > System.currentTimeMillis()) {
            int timeToSleep = (int) (slipIn - System.currentTimeMillis()) / 1000;
            //  description.setText(getStringTime(sec) + " c");
            minutesLeft = timeToSleep / 60;

            if (minutesLeft > 0) {
                int i = 0;
                while (shutDownTimers.size() > i && shutDownTimers.get(i) < minutesLeft) {
                    i += 1;
                }
                if (i < shutDownTimers.size()) {
                    alarmPosition = i;
                }

            }
            if (minutesLeft > 60) {
                text = minutesLeft / 60 + getResources().getString(R.string.hours);
            }
            text += minutesLeft % 60 + getResources().getString(R.string.minutes);

        } else {
            text = getResources().getString(R.string.off);
        }


        textView.setText(text);

        actions.put(view, new KeyActions()
                .addAction(KeyEvent.KEYCODE_DPAD_UP, () -> {
                    am.cancel(pi);
                    PreferenceManager.getDefaultSharedPreferences(this).edit().remove("shutDownTime").commit();
                    alarmPosition++;
                    setAlarm(am, pi, textView);
                })
                .addAction(KeyEvent.KEYCODE_DPAD_DOWN, () -> {
                    am.cancel(pi);
                    PreferenceManager.getDefaultSharedPreferences(this).edit().remove("shutDownTime").commit();
                    alarmPosition--;
                    setAlarm(am, pi, textView);

                })
        );
//        view.setOnClickListener((v) -> {
//
//            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//            Intent i = new Intent(AspectLayoutService.this, Alarm.class);
//
//            PendingIntent pi = PendingIntent.getService(AspectLayoutService.this, 0, i, 0);
//            am.set(AlarmManager.RTC_WAKEUP, 10000, pi);
//            Log.e("alarm", "click");
//
//        });
        view.setOnKeyListener(this);
    }

    private void addInputs(LinearLayout generalView) {
        View view = layoutInflater.inflate(R.layout.aspect_header_img, generalView, false);
        ((ImageView) view.findViewById(R.id.image)).setImageResource(R.drawable.ic_input_black_24dp);


        //((TextView) view.findViewById(R.id.text)).setText(R.string.inputs);
        generalView.addView(view);
        view.setOnFocusChangeListener((v, hasFocus) -> {
            bodyContainer.removeAllViews();
            bodyContainer.addView(bodyInputs);
            if (hasFocus) {
                description.setText(R.string.inputs);
            }
        });
        bodyInputs = (HorizontalScrollView) layoutInflater.inflate(R.layout.picture_settings_horizontal, bodyContainer, false);
        //iniInputs(bodyInputs.findViewById(R.id.container));
        iniInputs(bodyInputs.findViewById(R.id.container));
        List<View> list = new ArrayList<>();
        KeyListener listener = new KeyListener(list, view);
        LinearLayout container = ((LinearLayout) bodyInputs.findViewById(R.id.container));
        for (int i = 0; i < container.getChildCount(); i++) {
            //list.add(container.getChildAt(i));
            container.getChildAt(i).setOnKeyListener(listener);
        }
        view.setOnKeyListener(this);
        actions.put(view, new KeyActions()
                .addAction(KeyEvent.KEYCODE_DPAD_UP, () -> {
                    container.getChildAt(0).requestFocus();
                    container.getChildAt(0).requestFocus();
                }).setAction(KeyEvent.ACTION_UP)
        );
    }

    private void iniInputs(ViewGroup bodyInputs) {
        List<InputSourceHelper.INPUT_PORT> inputs = new InputSourceHelper().getPortsList(this);
        for (InputSourceHelper.INPUT_PORT port : inputs) {
            LinearLayout view = (LinearLayout) layoutInflater.inflate(R.layout.inputs, bodyInputs, false);
            ((TextView) view.findViewById(R.id.label)).setText(port.getNameResource());
            ((ImageView) view.findViewById(R.id.image)).setImageResource(port.getDrawable());
            view.setOnClickListener(v -> {
                new InputSourceHelper().changeInput(port.getId(), this);
                stopSelf();
            });
            view.setFocusable(true);
            bodyInputs.addView(view);
        }

    }

    View optimization;

    private void addOptimization(LinearLayout generalView) {
        View view = new View(this);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(1, 1);
        view.setLayoutParams(layoutParams);
        view.setFocusable(true);
        generalView.addView(view);
        optimization = layoutInflater.inflate(R.layout.aspect_header_img, generalView, false);
        ((ImageView) optimization.findViewById(R.id.image)).setImageResource(R.drawable.ic_speed_24dp);


        //((TextView) optimization.findViewById(R.id.text)).setText(R.string.optimization);
        generalView.addView(optimization);
        optimization.setOnKeyListener(this);
        optimization.setOnFocusChangeListener((v, hasFocus) -> {
            bodyContainer.removeAllViews();
            if (hasFocus) {
                description.setText(R.string.optimization);
            }
        });
        optimization.setOnClickListener((v) -> {
            Intent intent = new Intent("com.funshion.android.intent.action.FUN_TV_CC_BUTTON");
            optimization.getContext().sendBroadcast(intent);

        });
        optimization.requestFocus();
        actions.put(optimization, new KeyActions()
                .addAction(KeyEvent.KEYCODE_DPAD_LEFT, () -> {
                    if (isHaveChannel) {
                        channelHeader.requestFocus();
                    } else {
                        ratio.requestFocus();
                    }
                })
        );
    }

    private void addChannelSelector(LinearLayout generalView) {
        channelHeader = layoutInflater.inflate(R.layout.aspect_header_img, generalView, false);
        ((ImageView) channelHeader.findViewById(R.id.image)).setImageResource(R.drawable.channel_num_selected);

        // ((TextView) view.findViewById(R.id.text)).setText(R.string.channel);
        generalView.addView(channelHeader);
        channelHeader.setOnFocusChangeListener((v, hasFocus) -> {
            bodyContainer.removeAllViews();
            bodyContainer.addView(bodyChannel);
            if (hasFocus) {
                description.setText(R.string.channel);
            }

        });
        bodyChannel = new NumberDialing(this);
        initChanel();
        channelHeader.setOnKeyListener(this);

        actions.put(channelHeader, new KeyActions()
                        .addAction(KeyEvent.KEYCODE_DPAD_UP, () -> {
                            bodyChannel.request();
//                    View current = generalView.findFocus();
//                    if (current != null) current.clearFocus();
                            bodyChannel.request();
                        }).addAction(KeyEvent.KEYCODE_DPAD_RIGHT, () -> {
                            optimization.requestFocus();
                        })
        );


    }

    private void initChanel() {

    }

    View ratio;

    private void addRatio(LinearLayout generalView) {
        ratio = layoutInflater.inflate(R.layout.aspect_header, generalView, false);
        Ratio currentRatio = Ratio.getByID(pictureManager.getVideoArcType());
        if (currentRatio == null) {
            currentRatio = Ratio.VIDEO_ARC_DEFAULT;
        }
        ((TextView) ratio.findViewById(R.id.text)).setText(currentRatio.getString());
        generalView.addView(ratio);
        ratio.setOnFocusChangeListener((v, hasFocus) -> {
            bodyContainer.removeAllViews();
            if (hasFocus) {
                description.setText(R.string.ratio_desc);
            }
        });

        actions.put(ratio, new KeyActions()
                .addAction(KeyEvent.KEYCODE_DPAD_UP, () -> {
                    Ratio current = Ratio.getByID(pictureManager.getVideoArcType());
                    if (current == null) {
                        current = Ratio.VIDEO_ARC_DEFAULT;
                    }
                    int position = ratios.indexOf(current);
                    int newPosition = 0;
                    if (position < ratios.size() - 1) {
                        newPosition = position + 1;
                    }
                    current = ratios.get(newPosition);
                    pictureManager.setVideoArcType(current.getId());
                    ((TextView) ratio.findViewById(R.id.text)).setText(current.getString());
                })
                .addAction(KeyEvent.KEYCODE_DPAD_DOWN, () -> {
                    Ratio current = Ratio.getByID(pictureManager.getVideoArcType());
                    if (current == null) {
                        current = Ratio.VIDEO_ARC_DEFAULT;
                    }
                    int position = ratios.indexOf(current);
                    int newPosition = ratios.size() - 1;
                    if (position > 0) {
                        newPosition = position - 1;
                    }
                    current = ratios.get(newPosition);
                    pictureManager.setVideoArcType(current.getId());
                    ((TextView) ratio.findViewById(R.id.text)).setText(current.getString());
                })
        );
        if (!isHaveChannel) {
            actions.get(ratio)
                    .addAction(KeyEvent.KEYCODE_DPAD_RIGHT, () -> {

                        optimization.requestFocus();

                    });
        }


        ratio.setOnKeyListener(this);
    }

    HashMap<View, KeyActions> actions = new HashMap<>();


    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        lastUpdate = System.currentTimeMillis();
        if (keyCode == KeyEvent.KEYCODE_CHANNEL_UP)
            keyCode = KeyEvent.KEYCODE_DPAD_UP;
        if (keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN)
            keyCode = KeyEvent.KEYCODE_DPAD_DOWN;
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
            case KeyEvent.KEYCODE_MOVE_HOME:
            case KeyEvent.KEYCODE_TV_ZOOM_MODE:
            case KeyEvent.KEYCODE_BACK:
                if (event.getAction() == KeyEvent.ACTION_UP)
                    stopSelf();
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (actions.get(v) != null && actions.get(v).containsKeyCode(keyCode)) {
                    if (event.getAction() == actions.get(v).action)
                        actions.get(v).doAction(keyCode);
                    return true;
                } else return false;

            default:
                break;
        }
        return false;
    }

    public void setAlarm(AlarmManager alarm, PendingIntent pi, TextView textView) {
        if (alarmPosition < 0) {
            alarmPosition = shutDownTimers.size() - 1;
        }
        if (alarmPosition >= shutDownTimers.size()) {
            alarmPosition = 0;
        }
        minutesLeft = shutDownTimers.get(alarmPosition);
        if (minutesLeft > 0) {
            slipIn = System.currentTimeMillis() + minutesLeft * 60 * 1000;
            PreferenceManager.getDefaultSharedPreferences(this).edit().putLong("shutDownTime", slipIn).commit();

            String text = "";
            if (minutesLeft > 60) {
                text = minutesLeft / 60 + getString(R.string.hours);
            } else {
                text += minutesLeft + getString(R.string.minutes);
            }
            textView.setText(text);
            alarm.set(AlarmManager.RTC_WAKEUP, +slipIn, pi);
        } else {
            slipIn = -1;
            textView.setText(R.string.off);
        }
    }


    private boolean isPlay() {
        String string = getSystemProp("Storage_Video_Status", "Finalize");
        // Log.e("isSafe", "string " + string);
        return "inited".equals(string);
    }

    private boolean isSafe() {
        int mInputSource = TvCommonManager.getInstance().getCurrentTvInputSource();
        return !isUHD || (isPlay() || (mInputSource != TvCommonManager.INPUT_SOURCE_STORAGE));
    }

    private String getSystemProp(String prop, String value) {
        String string = "";
        try {

            Class properties = Class.forName("android.os.SystemProperties");
            Method setProp = properties.getMethod("get", new Class[]{String.class, String.class});
            string = (String) setProp.invoke(properties, new Object[]{prop, value});

        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }

    private class KeyActions {
        HashMap<Integer, Runnable> focusRunnable = new HashMap<>();
        int action = KeyEvent.ACTION_DOWN;

        public KeyActions setAction(int action) {
            this.action = action;
            return this;
        }

        KeyActions addAction(int keyCode, Runnable run) {
            focusRunnable.put(keyCode, run);
            return this;
        }

        void doAction(int keyCode) {
            focusRunnable.get(keyCode).run();
        }

        public boolean containsKeyCode(int keyCode) {
            return focusRunnable.containsKey(keyCode);
        }
    }

    String getStringTime(int time) {
        int minutes = time / (60 * 1000);
        int seconds = (time / 1000) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
