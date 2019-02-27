package com.wezom.kiviremoteserver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.wezom.kiviremoteserver.common.Constants;
import com.wezom.kiviremoteserver.environment.EnvironmentPictureSettings;
import com.wezom.kiviremoteserver.service.CursorService;


public class ScreenOnReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            context.startService(new Intent(context, CursorService.class));
            setInitialBackL(context);
        }
    }

    public static void setInitialBackL(Context context) {
        int backLight = PreferenceManager.getDefaultSharedPreferences(context).getInt(Constants.LAST_BACKLIGHT, Constants.NO_VALUE);
        if (backLight != Constants.NO_VALUE) {
            EnvironmentPictureSettings pictureSettings = new EnvironmentPictureSettings();
            pictureSettings.setBacklight(backLight, context);
        }
    }
}
