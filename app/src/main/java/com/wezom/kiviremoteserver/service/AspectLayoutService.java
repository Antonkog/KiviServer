package com.wezom.kiviremoteserver.service;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wezom.kiviremoteserver.App;
import com.wezom.kiviremoteserver.R;
import com.wezom.kiviremoteserver.environment.EnvironmentFactory;
import com.wezom.kiviremoteserver.environment.EnvironmentInputsHelper;
import com.wezom.kiviremoteserver.environment.EnvironmentPictureSettings;
import com.wezom.kiviremoteserver.service.aspect.Alarm;
import com.wezom.kiviremoteserver.service.aspect.HDRValues;
import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper;
import com.wezom.kiviremoteserver.ui.views.KeyListener;
import com.wezom.kiviremoteserver.ui.views.LRTextSwitcher;
import com.wezom.kiviremoteserver.ui.views.NumberDialing;
import com.wezom.kiviremoteserver.ui.views.ScreenProgress;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import wezom.kiviremoteserver.environment.bridge.BridgeGeneral;
import wezom.kiviremoteserver.environment.bridge.BridgePicture;
import wezom.kiviremoteserver.environment.bridge.driver_set.PictureMode;
import wezom.kiviremoteserver.environment.bridge.driver_set.Ratio;
import wezom.kiviremoteserver.environment.bridge.driver_set.SoundValues;
import wezom.kiviremoteserver.environment.bridge.driver_set.TemperatureValues;

//com.wezom.kiviremoteserver.mstar.java.com.wezom.kiviremoteserver.environment.bridge.driver_set.
//import wezom.kiviremoteserver.environment.bridge.driver_set.PictureMode;
//import wezom.kiviremoteserver.environment.bridge.driver_set.Ratio;
//Ratio

public class AspectLayoutService extends Service implements View.OnKeyListener {

    private final static String KEY_PIC_BRIGHTNESS = "psdBrightness";
    private final static String KEY_SOUND_DEPRECATED = "KEY_SOUND_DEPRECATED";
    private final static String KEY_PIC_CONTRAST = "psdContrast";
    private final static String KEY_PIC_SATURATION = "psdSaturation";
    private final static String KEY_PIC_HDR = "psdHDR";
    private final static String KEY_PIC_TEMPERATURE = "psdTemperature";
    private final static String KEY_PIC_BACKLIGHT = "psdBacklight";
    private static final String OSD_TIME = "OSD_TIME";
    private int generalType = BridgePicture.LAYER_TYPE;//WindowManager.LayoutParams.TYPE_TOAST;
    private WindowManager wmgr;
    private int alarmPosition = 0;
    private RelativeLayout generalView;
    private LinearLayout headerContainer;
    private LinearLayout bodyContainer;
    private LayoutInflater layoutInflater;
    // LinearLayout bodyPictureSettings;
    private HorizontalScrollView bodyInputs;
    private TextView description;
    private NumberDialing bodyChannel;
    //private View channelHeader;
    private EnvironmentPictureSettings pictureSettings;
    private EnvironmentInputsHelper inputsHelper;
    public static volatile long lastUpdate;
    private Handler timer = new Handler();
    private Runnable updateSleepTime = new Runnable() {
        @Override
        public void run() {
            if (sleepFocused) {
                updateSleepText();
            }

            timer.postDelayed(updateSleepTime, 1000);
            if (autoCloseTime > 0)
                if (System.currentTimeMillis() - autoCloseTime > lastUpdate) {
                    //Log.e("AspectLayoutService", "stopSelf " + System.currentTimeMillis() + ":" + autoCloseTime + ":" + lastUpdate);
                    stopSelf();
                }
        }


    };

    private void updateSleepText() {
        String timeStr = "";
        long leftTime = (slipIn - SystemClock.elapsedRealtime()) / 1000;
        // Log.e("AspectLayoutService", "leftTime " + leftTime);

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

//    Handler handler = new Handler();
//    Runnable runnable = new Runnable() {
//        @Override
//        public void run() {
//            View view = generalView.findFocus();
//            optimization.clearFocus();
//            if (view != null) {
//                Log.e("view", "" + view);
//             //   view.setBackgroundColor(Color.RED);
//            }
//            handler.postDelayed(runnable, 1000);
//        }
//    };

    //    PictureMode[] modes = ;

    //      PictureMode.PICTURE_MODE_ECONOMY);
    List<Ratio> ratios = Ratio.getInstance().getRatios();

    List<Integer> shutDownTimers = Arrays.asList(0, 15, 30, 60, 90, 120, 180);
    private boolean sleepFocused;
    // boolean isUHD = false;
    private static int mainColor = Color.BLUE;
    int autoCloseTime = 10;

    //android.widget.LinearLayout{e7580bb VFE...C.. .F...... 444,0-554,98 #7f090128 app:id/root}
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (App.checkWizard(this)) {
                return;
            }
        }
        autoCloseTime = Settings.Global.getInt(getContentResolver(), OSD_TIME, 10);

        if (autoCloseTime < 10 && autoCloseTime > 0) {
            autoCloseTime = 10;
        }
        autoCloseTime *= 1000;
        mainColor = getResources().getColor(R.color.colorPrimary);
        Log.e("AspectLayoutService", "started");

//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher_background);
//        mBuilder.setOnlyAlertOnce(true);
        //startForeground(0, mBuilder.build());

        // new Handler().postDelayed(() -> {
        String MODEL = Build.MODEL;
        // Log.e("MODEL", "name " + MODEL);
        // new Handler().postDelayed(() -> {
        lastUpdate = System.currentTimeMillis();
        pictureSettings = new EnvironmentPictureSettings();
        inputsHelper = new EnvironmentInputsHelper();
        createLayout(getBaseContext());
        //}, 5000);
        //if (autoCloseTime > 0)
        timer.postDelayed(updateSleepTime, autoCloseTime);
        //   }, 3000);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

//    boolean isHaveChannel = false;

    public void createLayout(Context context) {
        //Log.e("create", "create");

        layoutInflater = (LayoutInflater)
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        wmgr = (WindowManager) context.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        generalView = (RelativeLayout) View.inflate(context, R.layout.layout_aspect, null);
        //generalView.setVisibility(View.VISIBLE);
        final WindowManager.LayoutParams param = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                generalType,//TYPE_SYSTEM_ALERT
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT);
//        param.windowAnimations = android.R.style.Animation_Activity;
        //param.rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_SEAMLESS;
        // generalView.setVisibility(View.GONE);
        //      param.windowAnimations = android.R.style.Animation_Translucent;
        wmgr.addView(generalView, param);
//        generalView.setVisibility(View.VISIBLE);
//        generalView.clearAnimation();
//        generalView.animate().setInterpolator(new AccelerateDecelerateInterpolator())
//                .setDuration(500)
//                .translationY(400).start();
        // new Handler().postDelayed(()->generalView.setVisibility(View.VISIBLE),3000);
        headerContainer = generalView.findViewById(R.id.header_container);
        bodyContainer = generalView.findViewById(R.id.body_container);
        description = generalView.findViewById(R.id.description);
        generalView.setVisibility(View.VISIBLE);
        generalView.clearAnimation();


//        bodyContainer.animate().setInterpolator(new AccelerateDecelerateInterpolator())
//                .setDuration(500).translationYBy(0)
//                .translationY(0).start();
//        bodyContainer.animate()
//                .yBy(400).y(0)
//                .setStartDelay(100)
//                .setDuration(500).start();


        final Animation anDesk = AnimationUtils.loadAnimation(this, R.anim.outside_bottom);
        // anDesk.setInterpolator(new AccelerateDecelerateInterpolator());
        anDesk.setDuration(300);
        description.startAnimation(anDesk);
        bodyContainer.startAnimation(anDesk);
        headerContainer.startAnimation(anDesk);
//        final Animation anHead = AnimationUtils.loadAnimation(this, R.anim.outside_bottom);
//        anHead.setInterpolator(new AccelerateDecelerateInterpolator());
//        anHead.setDuration(200);
//        anHead.setStartTime(100);
//        final Animation anBody = AnimationUtils.loadAnimation(this, R.anim.outside_bottom);
//        anBody.setInterpolator(new AccelerateDecelerateInterpolator());
//        anBody.setDuration(200);
//        anHead.setStartTime(300);

        View view = new View(this);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(1, 1);
        view.setLayoutParams(layoutParams);
        view.setFocusable(true);
        headerContainer.addView(view);

        if (EnvironmentFactory.ENVIRONMENT_MTC == BridgeGeneral.ENVIRONMENT) {
            addSeparator(headerContainer);
            addOptimization(headerContainer);
        }
        addSeparator(headerContainer);
        addPictureSetting(headerContainer);
        addSeparator(headerContainer);
        if (EnvironmentFactory.ENVIRONMENT_REALTEC == BridgeGeneral.ENVIRONMENT) {
            addSoundSetting(headerContainer);
            addSeparator(headerContainer);
        }
//        addTemperatureSetting(headerContainer);
//        addSeparator(headerContainer);
        addInputs(headerContainer);
        addSeparator(headerContainer);
        addSleep(headerContainer);
//        addSeparator(headerContainer);
//        addPictureMode(headerContainer);
        addSeparator(headerContainer);
        addRatio(headerContainer);
        addSeparator(headerContainer);


        int current = inputsHelper.getCurrentTvInputSource();
        // Log.e("currentInput", "input = " + current);
        if (inputsHelper.isTV(current)) {
            addSeparator(headerContainer);
            addChannelSelector(headerContainer);
        }

        updateTextColors(generalView);
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

        initCarousel(headerContainer);
    }

    private void addSoundSetting(LinearLayout headerContainer) {
        View view = layoutInflater.inflate(R.layout.aspect_header_img, generalView, false);
        ((ImageView) view.findViewById(R.id.image)).setImageResource(R.drawable.ic_music_note_black_24dp);

        LinearLayout bodySoundSettings = (LinearLayout) layoutInflater.inflate(R.layout.picture_settings, bodyContainer, false);
        view.setOnFocusChangeListener((v, hasFocus) -> {
//
            if (hasFocus) {
                bodyContainer.removeAllViews();
                bodyContainer.addView(bodySoundSettings);
                description.setText(R.string.sound);
            }
        });
        soundDetailSettings(bodySoundSettings);
        List<View> list = new ArrayList<>();
        KeyListener listener = new KeyListener(list, view);
        for (int i = bodySoundSettings.getChildCount() - 1; i >= 0; i--) {
            list.add(bodySoundSettings.getChildAt(i));
            bodySoundSettings.getChildAt(i).setOnKeyListener(listener);
        }
        actions.put(view, new KeyActions()
                .addAction(KeyEvent.KEYCODE_DPAD_UP, (action) -> {
                    if (action == KeyEvent.ACTION_UP) {
                        list.get(0).requestFocus();
                        list.get(0).requestFocus();
                    }
                })
        );
        headerContainer.addView(view);
        view.setOnKeyListener(this);
    }

    ScreenProgress soundBass;
    ScreenProgress soundTreble;

    private void updateSoundValue(boolean isUser) {
        soundBass.setEnabled(isUser);
        soundBass.setFocusable(isUser);
        soundBass.setAlpha(isUser ? 1 : 0.3f);
        soundBass.setProgress(pictureSettings.getBassLevel(soundBass.getContext()));

        soundTreble.setEnabled(isUser);
        soundTreble.setFocusable(isUser);
        soundTreble.setAlpha(isUser ? 1 : 0.3f);
        soundTreble.setProgress(pictureSettings.getTrebleLevel(soundTreble.getContext()));
    }

    private void soundDetailSettings(LinearLayout bodyPictureSettings) {
        soundTreble = soundHeight(bodyPictureSettings);
        soundBass = soundLow(bodyPictureSettings);
        soundType(bodyPictureSettings);
    }

    private ScreenProgress soundHeight(LinearLayout body) {
        ScreenProgress screenProgress = new ScreenProgress(this);
        screenProgress.setOnKeyListener(this);
        screenProgress.setProgress(pictureSettings.getTrebleLevel(body.getContext()));

        screenProgress.setLable(R.string.sound_height);
        screenProgress.setIcon(R.drawable.ic_hight_sound);
        screenProgress.setProgressListener(progress ->
        {
            pictureSettings.setTrebleLevel(body.getContext(), progress);
        });
        body.addView(screenProgress);
        screenProgress.setKey(KEY_SOUND_DEPRECATED);
        return screenProgress;
    }

    private ScreenProgress soundLow(LinearLayout body) {
        ScreenProgress screenProgress = new ScreenProgress(this);
        screenProgress.setOnKeyListener(this);
        screenProgress.setProgress(pictureSettings.getBassLevel(body.getContext()));
        screenProgress.setLable(R.string.sound_low);
        screenProgress.setIcon(R.drawable.ic_low_sound);
        screenProgress.setProgressListener(progress ->
        {
            pictureSettings.setBassLevel(screenProgress.getContext(), progress);
        });
        body.addView(screenProgress);
        screenProgress.setKey(KEY_SOUND_DEPRECATED);
        return screenProgress;
    }

    private View soundType(LinearLayout body) {
        SoundValues current = SoundValues.getByID(pictureSettings.getSoundType());
        LRTextSwitcher lrTextSwitcher = new LRTextSwitcher(this);
        lrTextSwitcher.setUpValues(SoundValues.getSet());
        lrTextSwitcher.setOnKeyListener(this);
        if (current != null) {
            lrTextSwitcher.setValue(current);
            updateSoundValue(pictureSettings.isUserSoundMode());
        }
        lrTextSwitcher.setLable(R.string.sound_type);
        lrTextSwitcher.setIcon(R.drawable.ic_treble_24dp);
        lrTextSwitcher.setProgressListener(progress -> {
            pictureSettings.setSoundType(progress);
            updateSoundValue(progress == SoundValues.SOUND_TYPE_USER.getID());
        });
        body.addView(lrTextSwitcher);
        lrTextSwitcher.setKey(KEY_SOUND_DEPRECATED);
        return lrTextSwitcher;
    }

    private void initCarousel(LinearLayout headerContainer) {
        int i = 0;
        KeyActions keyActions = actions.get(headerContainer.getChildAt(0));
        if (keyActions == null) {
            i = 1;
            keyActions = actions.get(headerContainer.getChildAt(1));
        }
        if (keyActions == null) {
            i = 2;
            keyActions = actions.get(headerContainer.getChildAt(2));
        }
        if (keyActions != null) {
            headerContainer.getChildAt(i).requestFocus();
            headerContainer.getChildAt(i).requestFocus();
            keyActions.addAction(KeyEvent.KEYCODE_DPAD_LEFT, (action) -> {
                if (action == KeyEvent.ACTION_DOWN)
                    headerContainer.getChildAt(headerContainer.getChildCount() - 1).requestFocus();
            });
        }
        keyActions = actions.get(headerContainer.getChildAt(headerContainer.getChildCount() - 1));
        if (keyActions != null) {
            int finalI = i;
            keyActions
                    .addAction(KeyEvent.KEYCODE_DPAD_RIGHT, (action) -> {
                        if (action == KeyEvent.ACTION_DOWN)
                            headerContainer.getChildAt(finalI).requestFocus();
                    });
        }
    }

    public static void updateTextColors(ViewGroup generalView) {
        for (int i = 0; i < generalView.getChildCount(); i++) {
            View view = generalView.getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(Color.WHITE);
            }
            if (view instanceof ViewGroup) {
                updateTextColors((ViewGroup) view);
            }
        }
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
        try {
            timer.removeCallbacks(updateSleepTime);
        } catch (Exception e) {

        }
        if (wmgr != null)
            wmgr.removeView(generalView);
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
        //updateTextColors(bodyPictureSettings);
        pictureDetailSettings(bodyPictureSettings);
        List<View> list = new ArrayList<>();
        KeyListener listener = new KeyListener(list, view);
        for (int i = bodyPictureSettings.getChildCount() - 1; i >= 0; i--) {
            list.add(bodyPictureSettings.getChildAt(i));
            bodyPictureSettings.getChildAt(i).setOnKeyListener(listener);
        }
        actions.put(view, new KeyActions()
                .addAction(KeyEvent.KEYCODE_DPAD_UP, (action) -> {
                    if (action == KeyEvent.ACTION_UP) {
                        list.get(0).requestFocus();
                        list.get(0).requestFocus();
                    }
                })
        );
        generalView.addView(view);
        view.setOnKeyListener(this);
    }

    int contrast = 0;
    int brightness = 0;
    int sharpness = 0;
    int saturation = 0;
    int backlight = 0;
    int color_r = 0;
    int color_g = 0;
    int color_b = 0;

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
        pictureSettings.initColors();
        color_r = pictureSettings.getRedColor();
        color_g = pictureSettings.getGreenColor();
        color_b = pictureSettings.getBlueColor();


        pdsColorTemperature(bodyPictureSettings);//
        psdRed(bodyPictureSettings);//??
        psdBlue(bodyPictureSettings);//??
        psdGreen(bodyPictureSettings);//??
        psdHDR(bodyPictureSettings);//??


        generalView.addView(view);
        view.setOnKeyListener(this);

    }

    List<View> picturesView = new ArrayList<>();

    private void pictureDetailSettings(LinearLayout body) {

        pictureSettings.initSettings(this);

        brightness = pictureSettings.getBrightness();
        contrast = pictureSettings.getContrast();
        sharpness = pictureSettings.getSharpness();
        saturation = pictureSettings.getSaturation();
        backlight = pictureSettings.getBacklight();
        if (isSafe()) {
            picturesView.add(psdBrightness(body));//+
            picturesView.add(psdContrast(body));//+
            picturesView.add(psdSaturation(body));//+
            //psdSharpness(body);//+
            if (BridgeGeneral.ENVIRONMENT != EnvironmentFactory.ENVIRONMENT_REALTEC)
                picturesView.add(psdHDR(body));//+
            picturesView.add(psdTemperature(body));//+
            addPictureModeRow(body);//+
        }
        psdBacklight(body);//+
    }

    private View psdTemperature(LinearLayout body) {
        TemperatureValues current = TemperatureValues.getByID(pictureSettings.getTemperature());
        LRTextSwitcher lrTextSwitcher = new LRTextSwitcher(this);
        lrTextSwitcher.setUpValues(TemperatureValues.getSet());
        lrTextSwitcher.setOnKeyListener(this);
        if (current != null)
            lrTextSwitcher.setValue(current);
        lrTextSwitcher.setLable(R.string.temperature_des);
        lrTextSwitcher.setIcon(R.drawable.color_temper_focus);
        lrTextSwitcher.setProgressListener(progress -> pictureSettings.setTemperature(progress));
        body.addView(lrTextSwitcher);
        lrTextSwitcher.setKey(KEY_PIC_TEMPERATURE);
        return lrTextSwitcher;
    }

    private View psdHDR(LinearLayout body) {
        LRTextSwitcher lrTextSwitcher = new LRTextSwitcher(this);
        HDRValues current = HDRValues.getByID(pictureSettings.getHDR());
        lrTextSwitcher.setUpValues(pictureSettings.getHDRSet());
        lrTextSwitcher.setOnKeyListener(this);
        if (current != null)
            lrTextSwitcher.setValue(current);
        lrTextSwitcher.setLable(R.string.hdr);
        lrTextSwitcher.setIcon(R.drawable.ic_hdr_strong_black_24dp);
        lrTextSwitcher.setProgressListener(progress -> {
            pictureSettings.setHDR(progress);
        });
        body.addView(lrTextSwitcher);
        lrTextSwitcher.setKey(KEY_PIC_HDR);
        return lrTextSwitcher;
    }

    private void psdGreen(LinearLayout body) {
        ScreenProgress screenProgress = new ScreenProgress(this);
        screenProgress.setOnKeyListener(this);
        screenProgress.setProgress(color_g);
        screenProgress.setLable(R.string.color_g);
        screenProgress.setIcon(R.drawable.ic_color_lens_black_24dp);
        screenProgress.setProgressListener(progress -> pictureSettings.setGreen(progress));
        body.addView(screenProgress);
    }

    private void psdBlue(LinearLayout body) {
        ScreenProgress screenProgress = new ScreenProgress(this);
        screenProgress.setOnKeyListener(this);
        screenProgress.setProgress(color_b);
        screenProgress.setLable(R.string.color_b);
        screenProgress.setIcon(R.drawable.ic_color_lens_black_24dp);
        screenProgress.setProgressListener(progress -> pictureSettings.setBLue(progress));
        body.addView(screenProgress);
    }

    private void psdRed(LinearLayout body) {
        ScreenProgress screenProgress = new ScreenProgress(this);
        screenProgress.setOnKeyListener(this);
        screenProgress.setProgress(color_r);
        screenProgress.setLable(R.string.color_r);
        screenProgress.setIcon(R.drawable.ic_color_lens_black_24dp);
        screenProgress.setProgressListener(progress -> pictureSettings.setRed(progress));
        body.addView(screenProgress);
    }

    private void psdSharpness(LinearLayout body) {
        ScreenProgress screenProgress = new ScreenProgress(this);
        screenProgress.setOnKeyListener(this);
        screenProgress.setProgress(sharpness);
        screenProgress.setLable(R.string.sharpness);
        screenProgress.setIcon(R.drawable.sharpness_focus);
        screenProgress.setProgressListener(progress -> pictureSettings.setSharpness(progress));
        body.addView(screenProgress);
    }

    private View psdSaturation(LinearLayout body) {
        ScreenProgress screenProgress = new ScreenProgress(this);
        screenProgress.setOnKeyListener(this);
        screenProgress.setProgress(saturation);
        screenProgress.setLable(R.string.saturation);
        screenProgress.setIcon(R.drawable.saturation_focus);
        screenProgress.setProgressListener(progress ->
                pictureSettings.setSaturation(progress));
        body.addView(screenProgress);
        screenProgress.setKey(KEY_PIC_SATURATION);
        return screenProgress;
    }

    private View psdContrast(LinearLayout body) {
        ScreenProgress screenProgress = new ScreenProgress(this);
        screenProgress.setOnKeyListener(this);
        screenProgress.setProgress(contrast);
        screenProgress.setLable(R.string.contrast);
        screenProgress.setIcon(R.drawable.contrast_focus);
        screenProgress.setProgressListener(progress ->
                pictureSettings.setContrast(progress));
        body.addView(screenProgress);
        screenProgress.setKey(KEY_PIC_CONTRAST);
        return screenProgress;
    }

    private View psdBrightness(LinearLayout body) {
        ScreenProgress screenProgress = new ScreenProgress(this);
        screenProgress.setOnKeyListener(this);
        screenProgress.setProgress(brightness);
        screenProgress.setLable(R.string.brightness);
        screenProgress.setIcon(R.drawable.bright_focus);
        screenProgress.setProgressListener(progress ->
                pictureSettings.setBrightness(progress));
        body.addView(screenProgress);
        screenProgress.setKey(KEY_PIC_BRIGHTNESS);
        return screenProgress;
    }

    private void psdBacklight(LinearLayout body) {
        ScreenProgress screenProgress = new ScreenProgress(this);
        screenProgress.setOnKeyListener(this);
        screenProgress.setProgress(backlight);
        screenProgress.setLable(R.string.backlight);
        screenProgress.setIcon(R.drawable.backlight_focus);
        screenProgress.setProgressListener(progress ->
                pictureSettings.setBacklight(progress, getBaseContext()));
        body.addView(screenProgress);
//        screenProgress.setKey(KEY_PIC_BACKLIGHT);
//        return screenProgress;
    }

    private void pdsColorTemperature(LinearLayout body) {
        //TvPictureManager.getInstance().setColorTempratureEx();
    }

    private void addPictureModeRow(LinearLayout body) {
        LRTextSwitcher lrTextSwitcher = new LRTextSwitcher(this);
        PictureMode current = PictureMode.getByID(pictureSettings.getPictureMode());
        PictureMode[] picArr = new PictureMode[PictureMode.getModes().size()];
        lrTextSwitcher.setUpValues(PictureMode.getModes().toArray(picArr));
        lrTextSwitcher.setOnKeyListener(this);
        if (current != null) {
            lrTextSwitcher.setValue(current);
            enablePicturesSettings(current == PictureMode.PICTURE_MODE_USER, false);
        }
        lrTextSwitcher.setLable(R.string.picture_mode);
        lrTextSwitcher.setIcon(R.drawable.ic_image_w_24dp);
        lrTextSwitcher.setProgressListener(progress -> {
            pictureSettings.setPictureMode(progress);
            enablePicturesSettings(progress == PictureMode.PICTURE_MODE_USER.getID(), true);
        });
        body.addView(lrTextSwitcher);
    }

    private void enablePicturesSettings(boolean enable, boolean updateData) {
        if (updateData)
            pictureSettings.initSettings(this);
        for (View v : picturesView) {
            v.setEnabled(enable);
            v.setFocusable(enable);
            v.setAlpha(enable ? 1 : 0.3f);
            updateViewData(v);
        }
    }

    private void updateViewData(View v) {
        if (v instanceof ScreenProgress) {
            ScreenProgress screenProgress = (ScreenProgress) v;
            switch (screenProgress.getKey()) {
                case KEY_PIC_BACKLIGHT:
                    screenProgress.setProgress(pictureSettings.getBacklight());
                    break;
                case KEY_PIC_BRIGHTNESS:
                    screenProgress.setProgress(pictureSettings.getBrightness());
                    break;
                case KEY_PIC_CONTRAST:
                    screenProgress.setProgress(pictureSettings.getContrast());
                    break;
                case KEY_PIC_SATURATION:
                    screenProgress.setProgress(pictureSettings.getSaturation());
                    break;
            }
        } else if (v instanceof LRTextSwitcher) {
            LRTextSwitcher lrTextSwitcher = (LRTextSwitcher) v;
            switch (lrTextSwitcher.getKey()) {
                case KEY_PIC_HDR:
                    HDRValues current = HDRValues.getByID(pictureSettings.getHDR());
                    if (current != null)
                        lrTextSwitcher.setValue(current);
                    break;
                case KEY_PIC_TEMPERATURE:
                    TemperatureValues temp = TemperatureValues.getByID(pictureSettings.getTemperature());
                    if (temp != null)
                        lrTextSwitcher.setValue(temp);
                    break;
            }
        }
    }


    private void addPictureMode(LinearLayout generalView) {
        View view = layoutInflater.inflate(R.layout.aspect_header, generalView, false);
        PictureMode currentMode = PictureMode.getByID(pictureSettings.getPictureMode());
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
                .addAction(KeyEvent.KEYCODE_DPAD_UP, (action) -> {
                    if (action == KeyEvent.ACTION_DOWN)
                        if (isSafe()) {
                            PictureMode current = PictureMode.getByID(pictureSettings.getPictureMode());
                            if (current == null) {
                                current = PictureMode.PICTURE_MODE_NORMAL;
                            }
                            int position = PictureMode.getModes().indexOf(current);
                            int newPosition = 0;
                            if (position < PictureMode.getModes().size() - 1) {
                                newPosition = position + 1;
                            }
                            current = PictureMode.getModes().get(newPosition);
                            pictureSettings.setPictureMode(current.getID());
                            ((TextView) view.findViewById(R.id.text)).setText(current.getString());
                        }
                })
                .addAction(KeyEvent.KEYCODE_DPAD_DOWN, (action) -> {
                    if (action == KeyEvent.ACTION_DOWN)
                        if (isSafe()) {
                            PictureMode current = PictureMode.getByID(pictureSettings.getPictureMode());
                            if (current == null) {
                                current = PictureMode.PICTURE_MODE_NORMAL;
                            }
                            int position = PictureMode.getModes().indexOf(current);
                            int newPosition = PictureMode.getModes().size() - 1;
                            if (position > 0) {
                                newPosition = position - 1;
                            }
                            current = PictureMode.getModes().get(newPosition);
                            pictureSettings.setPictureMode(current.getID());
                            ((TextView) view.findViewById(R.id.text)).setText(current.getString());
                        }
                })
        );


    }

    int minutesLeft;

    String getStringTime(int time) {
        int minutes = time / (60 * 1000);
        int seconds = (time / 1000) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    long slipIn = 0;


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
        //TODO AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(AspectLayoutService.this, Alarm.class);
        PendingIntent pi = PendingIntent.getService(AspectLayoutService.this, 0, intent, 0);
        //  slipIn = PreferenceManager.getDefaultSharedPreferences(this).getLong("shutDownTime", -1);
        slipIn = Settings.Global.getLong(getContentResolver(), SLEEP_TIMEOUT_REMAIN, SystemClock.elapsedRealtime());

        String text = "";
        if (slipIn > SystemClock.elapsedRealtime()) {
            int timeToSleep = (int) (slipIn - SystemClock.elapsedRealtime()) / 1000;
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
            text += String.format("%02d", minutesLeft % 60) + getResources().getString(R.string.minutes);

        } else {
            text = getResources().getString(R.string.off);
        }


        textView.setText(text);

        actions.put(view, new KeyActions()
                .addAction(KeyEvent.KEYCODE_DPAD_UP, (action) -> {
                    if (action == KeyEvent.ACTION_DOWN) {
                        // am.cancel(pi);
                        PreferenceManager.getDefaultSharedPreferences(this).edit().remove("shutDownTime").commit();
                        alarmPosition++;
                        setAlarm(pi, textView);
                    }
                })
                .addAction(KeyEvent.KEYCODE_DPAD_DOWN, (action) -> {
                    if (action == KeyEvent.ACTION_DOWN) {
                        //  am.cancel(pi);
                        PreferenceManager.getDefaultSharedPreferences(this).edit().remove("shutDownTime").commit();
                        alarmPosition--;
                        setAlarm(pi, textView);
                    }

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
        //  updateTextColors(bodyInputs);
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
                .addAction(KeyEvent.KEYCODE_DPAD_UP, (action) -> {
                    if (action == KeyEvent.ACTION_UP) {
                        container.getChildAt(0).requestFocus();
                        container.getChildAt(0).requestFocus();
                    }
                })
        );
    }

    private static SoftReference<TextView> hdmi1;
    private static SoftReference<TextView> hdmi2;
    private static SoftReference<TextView> hdmi3;

    public static void updateIfNeeded(boolean h1, boolean h2, boolean h3) {
        if (hdmi1 != null) {
            TextView v1 = hdmi1.get();
            TextView v2 = hdmi2.get();
            TextView v3 = hdmi3.get();
            if (h1) {
                v1.setTextColor(mainColor);
            } else {
                v1.setTextColor(Color.WHITE);
            }
            if (h2) {
                v2.setTextColor(mainColor);
            } else {
                v2.setTextColor(Color.WHITE);
            }
            if (h3) {
                v3.setTextColor(mainColor);
            } else {
                v3.setTextColor(Color.WHITE);
            }
        }
    }

    private void iniInputs(ViewGroup bodyInputs) {
        List<InputSourceHelper.INPUT_PORT> inputs = new InputSourceHelper().getPortsList(this);
        for (InputSourceHelper.INPUT_PORT port : inputs) {
            LinearLayout view = (LinearLayout) layoutInflater.inflate(R.layout.inputs, bodyInputs, false);
            ((TextView) view.findViewById(R.id.label)).setText(port.getNameResource());
            if (port.isConnected()) {
                ((TextView) view.findViewById(R.id.label)).setTextColor(Color.GREEN);
            }
            if (port == InputSourceHelper.INPUT_PORT.INPUT_SOURCE_HDMI) {
                hdmi1 = new SoftReference<>(view.findViewById(R.id.label));
            }
            if (port == InputSourceHelper.INPUT_PORT.INPUT_SOURCE_HDMI2) {
                hdmi2 = new SoftReference<>(view.findViewById(R.id.label));
            }
            if (port == InputSourceHelper.INPUT_PORT.INPUT_SOURCE_HDMI3) {
                hdmi3 = new SoftReference<>(view.findViewById(R.id.label));
            }
            ((ImageView) view.findViewById(R.id.image)).setImageResource(port.getDrawable());
            view.setOnClickListener(v -> {
                new InputSourceHelper().changeInput(port.getId(), this);
                stopSelf();
            });
            view.setFocusable(true);
            bodyInputs.addView(view);
        }

    }

//    View optimization;

    private void addOptimization(LinearLayout generalView) {

        View optimization = layoutInflater.inflate(R.layout.aspect_header_img, generalView, false);
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
        actions.put(optimization, new KeyActions()
                .addAction(KeyEvent.KEYCODE_DPAD_LEFT, (action) -> {
                })
        );

        optimization.setOnClickListener((v) -> {
            Intent intent = new Intent("com.funshion.android.intent.action.FUN_TV_CC_BUTTON");
            optimization.getContext().sendBroadcast(intent);
//            Intent intentSettings = new Intent("android.settings.SETTINGS");
//            intentSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//            startActivity(intentSettings);

        });


    }

    private void addChannelSelector(LinearLayout generalView) {
        View channelHeader = layoutInflater.inflate(R.layout.aspect_header_img, generalView, false);
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
                        .addAction(KeyEvent.KEYCODE_DPAD_UP, (action) -> {
                            if (action == KeyEvent.ACTION_DOWN) {
                                bodyChannel.request();
//                    View current = generalView.findFocus();
//                    if (current != null) current.clearFocus();
                                bodyChannel.request();
                            }
                        })
        );


    }

    private void initChanel() {

    }

//    View ratio;

    private void addRatio(LinearLayout generalView) {
        View ratio = layoutInflater.inflate(R.layout.aspect_header, generalView, false);
        Ratio currentRatio = Ratio.getByID(pictureSettings.getVideoArcType());
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
                .addAction(KeyEvent.KEYCODE_DPAD_UP, (action) -> {
                    if (action == KeyEvent.ACTION_DOWN) {
                        Ratio current = Ratio.getByID(pictureSettings.getVideoArcType());
                        if (current == null) {
                            current = Ratio.VIDEO_ARC_DEFAULT;
                        }
                        int position = ratios.indexOf(current);
                        int newPosition = 0;
                        if (position < ratios.size() - 1) {
                            newPosition = position + 1;
                        }
                        current = ratios.get(newPosition);
                        pictureSettings.setVideoArcType(current.getId());
                        ((TextView) ratio.findViewById(R.id.text)).setText(current.getString());
                    }
                })
                .addAction(KeyEvent.KEYCODE_DPAD_DOWN, (action) -> {
                    if (action == KeyEvent.ACTION_DOWN) {
                        Ratio current = Ratio.getByID(pictureSettings.getVideoArcType());
                        if (current == null) {
                            current = Ratio.VIDEO_ARC_DEFAULT;
                        }
                        int position = ratios.indexOf(current);
                        int newPosition = ratios.size() - 1;
                        if (position > 0) {
                            newPosition = position - 1;
                        }
                        current = ratios.get(newPosition);
                        pictureSettings.setVideoArcType(current.getId());
                        ((TextView) ratio.findViewById(R.id.text)).setText(current.getString());
                    }
                })
        );


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
                    actions.get(v).doAction(keyCode, event.getAction());
                    return true;
                } else return false;

            default:
                break;
        }
        return false;
    }

    public void setAlarm(/*AlarmManager alarm,*/ PendingIntent pi, TextView textView) {
        if (alarmPosition < 0) {
            alarmPosition = shutDownTimers.size() - 1;
        }
        if (alarmPosition >= shutDownTimers.size()) {
            alarmPosition = 0;
        }
        minutesLeft = shutDownTimers.get(alarmPosition);
        if (minutesLeft > 0) {
            slipIn = SystemClock.elapsedRealtime() + minutesLeft * 60 * 1000;
            PreferenceManager.getDefaultSharedPreferences(this).edit().putLong("shutDownTime", slipIn).commit();

            String text = "";
            if (minutesLeft > 60) {
                text = minutesLeft / 60 + getString(R.string.hours) + String.format("%02d", minutesLeft % 60) + getString(R.string.minutes);
            } else {
                text += minutesLeft + getString(R.string.minutes);
            }
            textView.setText(text);
            sleepIn(slipIn, minutesLeft * 60 * 1000);
            //alarm.set(AlarmManager.RTC_WAKEUP, +slipIn, pi);
        } else {
            slipIn = -1;
            sleepIn(slipIn, 0);
            textView.setText(R.string.off);
        }
    }

    private static final String SHUTDOWN_INTENT_EXTRA = "shutdown";
    public static final String SLEEP_TIMEOUT = "sleep_timer";
    public static final String SLEEP_TIMEOUT_REMAIN = "sleep_timer_remain";
    public static final String AUTO_POWER_DOWN_TIMEOUT = "auto_power_down_timer";

    private void sleepIn(long timeIn, int delay) {
        Settings.Global.putLong(getContentResolver(), SLEEP_TIMEOUT_REMAIN, SystemClock.elapsedRealtime() + delay);
        Settings.Global.putInt(getContentResolver(), SLEEP_TIMEOUT, delay / (60 * 1000));
        Intent intentForToast = new Intent();
        intentForToast.setComponent(new ComponentName("com.hikeen.menu", "com.hikeen.menu.util.SleepTimeRecevier"));
        intentForToast.setAction("com.kivi.sleep.action");
        sendBroadcast(intentForToast);
    }

    private boolean isSafe() {
        return pictureSettings.isSafe();
    }


    private class KeyActions {
        HashMap<Integer, ActionCode> focusRunnable = new HashMap<>();
//        int action = KeyEvent.ACTION_DOWN;
//
//        public KeyActions setAction(int action) {
//            this.action = action;
//            return this;
//        }

        KeyActions addAction(int keyCode, ActionCode run) {
            focusRunnable.put(keyCode, run);
            return this;
        }

        void doAction(int keyCode, int actionCode) {
            focusRunnable.get(keyCode).doAction(actionCode);
        }

        public boolean containsKeyCode(int keyCode) {
            return focusRunnable.containsKey(keyCode);
        }
    }

    interface ActionCode {
        void doAction(int action);
    }
}
