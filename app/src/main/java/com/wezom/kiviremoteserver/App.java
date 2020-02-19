package com.wezom.kiviremoteserver;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.inputmethod.pinyin.util.PropertyHelper;
import com.wezom.kiviremoteserver.common.Constants;
import com.wezom.kiviremoteserver.di.components.ApplicationComponent;
import com.wezom.kiviremoteserver.di.components.DaggerApplicationComponent;
import com.wezom.kiviremoteserver.di.modules.ApplicationModule;
import com.wezom.kiviremoteserver.receiver.ScreenOnReceiver;
import com.wezom.kiviremoteserver.service.AspectLayoutService;
import com.wezom.kiviremoteserver.service.CursorService;
import com.wezom.kiviremoteserver.service.RemoteConlrolService;
import com.wezom.kiviremoteserver.service.communication.DelegatePlatformsService;
import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper;

import java.util.List;
import java.util.UUID;

import timber.log.Timber;

import static com.wezom.kiviremoteserver.common.Constants.APPLICATION_UID;

/**
 * Created by andre on 19.05.2017.
 */

public class App extends Application {
    private static ApplicationComponent appComponent;
    private static Context context;
    SharedPreferences prefs;
    public static volatile boolean hdmiStatus1;
    public static volatile boolean hdmiStatus2;
    public static volatile boolean hdmiStatus3;


    public static ApplicationComponent getApplicationComponent() {
        return appComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this.getBaseContext();

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String uid = prefs.getString(APPLICATION_UID, null);

        Timber.e("APP:  current app version: " + BuildConfig.VERSION_NAME + " debug? : "+ BuildConfig.DEBUG);

        if (uid == null) {
            prefs.edit().putString(APPLICATION_UID, UUID.randomUUID().toString().substring(3, 7)).apply();
        }

        appComponent = DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        if (!"release".equals(BuildConfig.BUILD_TYPE)) {
            Timber.plant(new Timber.DebugTree());
        }

        startService(new Intent(this, RemoteConlrolService.class));
        startService(new Intent(this, CursorService.class));

        if (isTVRealtek()) {
            BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    Log.e("inputManager", "usb " + action + "  : " + device);
                    startDialog(/*device*/TYPE_USB, 0, App.context);
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            registerReceiver(mUsbReceiver, filter);
        }

        ScreenOnReceiver.setInitialBackL(getBaseContext());
    }

    private final static int TYPE_USB = 0;
    private final static int TYPE_HDMI = 1;

    public static void hdmiStatusChanged(int id) {
        //  context.sendBroadcast();
        AspectLayoutService.updateIfNeeded(hdmiStatus1, hdmiStatus2, hdmiStatus3);
        DelegatePlatformsService.sendInputsList(context);
        if (id > 0) {
            startDialog(TYPE_HDMI, id, context);
        }
    }

    private static void startDialog(int type, int id, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkWizard(context)) {
                return;
            }
        }
        WindowManager wmgr = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        final WindowManager.LayoutParams param = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG,//TYPE_SYSTEM_ALERT
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT);
        param.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        param.windowAnimations = android.R.style.Animation_Toast;

        View generalView = View.inflate(context, R.layout.layout_dialog, null);
        if (type == TYPE_USB) {
            ((ImageView) generalView.findViewById(R.id.image)).setImageResource(R.drawable.ic_usb);
            ((TextView) generalView.findViewById(R.id.text)).setText("USB");
        } else if (type == TYPE_HDMI) {
            ((ImageView) generalView.findViewById(R.id.image)).setImageResource(R.drawable.ic_ser_hdmi);
            ((TextView) generalView.findViewById(R.id.text)).setText("HDMI");
        }
        generalView.findViewById(R.id.yes).setOnClickListener(v -> {
            if (type == TYPE_USB) {
                try {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.hikeen.mediabrowser",
                            "com.hikeen.mediabrowser.activity.MediaBrowser"));
                    context.startActivity(intent);
                }catch (ActivityNotFoundException e){
                    Timber.e(new Throwable("com.hikeen.mediabrowser not found" + Build.MODEL + Build.BRAND));
                }catch (Exception e){
                    Timber.e(new Throwable("com.hikeen.mediabrowser starting trouble" + Build.MODEL + Build.BRAND));
                }
                // wmgr.removeView(generalView);
            } else if (type == TYPE_HDMI) {
                int port = InputSourceHelper.INPUT_PORT.INPUT_SOURCE_HDMI.getId();
                switch (id) {
                    case 1:
                        port = InputSourceHelper.INPUT_PORT.INPUT_SOURCE_HDMI.getId();
                        break;
                    case 2:
                        port = InputSourceHelper.INPUT_PORT.INPUT_SOURCE_HDMI2.getId();
                        break;
                    case 3:
                        port = InputSourceHelper.INPUT_PORT.INPUT_SOURCE_HDMI3.getId();
                        break;
                }
                new InputSourceHelper().changeInput(port, context);

            }
            wmgr.removeView(generalView);
        });

        generalView.findViewById(R.id.no).setOnClickListener(v -> {
            wmgr.removeView(generalView);
        });
        wmgr.addView(generalView, param);
        new Handler().postDelayed(() -> {
            try {

                wmgr.removeView(generalView);
            } catch (Exception e) {
                Timber.e(e);
            }
        }, 30 * 1000);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean checkWizard(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            final List<ActivityManager.RunningAppProcessInfo> runningProcesses = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (Constants.PKG_SETUP_WIZARD.equals(activeProcess)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    public static boolean isTVRealtek() {
        return "realtek".equalsIgnoreCase(PropertyHelper.getProperty("ro.product.manufacturer"));
    }


}
