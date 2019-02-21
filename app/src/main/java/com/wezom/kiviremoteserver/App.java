package com.wezom.kiviremoteserver;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;
import com.wezom.kiviremoteserver.common.Constants;
import com.wezom.kiviremoteserver.di.components.ApplicationComponent;
import com.wezom.kiviremoteserver.di.components.DaggerApplicationComponent;
import com.wezom.kiviremoteserver.di.modules.ApplicationModule;
import com.wezom.kiviremoteserver.environment.EnvironmentPictureSettings;
import com.wezom.kiviremoteserver.service.CursorService;
import com.wezom.kiviremoteserver.service.KiviRemoteService;

import java.lang.reflect.Method;
import java.util.UUID;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;
import wezom.kiviremoteserver.environment.bridge.BridgeGeneral;

import static com.wezom.kiviremoteserver.common.Constants.APPLICATION_UID;

/**
 * Created by andre on 19.05.2017.
 */

public class App extends Application {
    private static ApplicationComponent appComponent;
    SharedPreferences prefs;

    public static ApplicationComponent getApplicationComponent() {
        return appComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String uid = prefs.getString(APPLICATION_UID, null);

        if (uid == null) {
            prefs.edit().putString(APPLICATION_UID, UUID.randomUUID().toString().substring(3, 7)).apply();
        }

        appComponent = DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        if ("release".equals(BuildConfig.BUILD_TYPE)) {
            Fabric.with(this, new Crashlytics());
        } else {
            Timber.plant(new Timber.DebugTree());
        }

        startService(new Intent(this, KiviRemoteService.class));
        startService(new Intent(this, CursorService.class));
        if (isTVRealtek()) {
            BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    Log.e("inputManager", "usb " + action + "  : " + device);
                    startDialog(device);
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            registerReceiver(mUsbReceiver, filter);
        }

        setInitialTvValues();

    }

    private void setInitialTvValues() {
        int progress = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getInt(Constants.LAST_BRIGHTNESS, Constants.NO_VALUE);
        if(progress != Constants.NO_VALUE){
            EnvironmentPictureSettings pictureSettings =  new EnvironmentPictureSettings();
            pictureSettings.setBrightness(progress, getBaseContext());
        }
    }

    private void startDialog(UsbDevice device) {
        WindowManager wmgr = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        final WindowManager.LayoutParams param = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG,//TYPE_SYSTEM_ALERT
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT);
        param.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        param.windowAnimations = android.R.style.Animation_Toast;
        View generalView = View.inflate(this, R.layout.layout_dialog, null);
        generalView.findViewById(R.id.yes).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.rtk.mediabrowser", "com.rtk.mediabrowser.MediaBrowser"));
            startActivity(intent);
            wmgr.removeView(generalView);
        });

        generalView.findViewById(R.id.no).setOnClickListener(v -> {
            wmgr.removeView(generalView);
        });
        wmgr.addView(generalView, param);
        new android.os.Handler().postDelayed(() -> {
            try {

                wmgr.removeView(generalView);
            } catch (Exception e) {
            }
            ;
        }, 30 * 1000);
    }


    public static boolean isTVRealtek() {
        return "realtek".equalsIgnoreCase(getProperty("ro.product.manufacturer"));
    }

    public static String getProperty(String value) {
        String model = "";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            model = (String) get.invoke(c, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }

    public static boolean isRUMarket() {
        try {
            return new BridgeGeneral().isRUMarket();
        } catch (Exception e) {
            return true;
        }
        // return MSTAR();
    }
}
