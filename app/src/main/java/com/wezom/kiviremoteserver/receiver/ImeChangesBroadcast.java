package com.wezom.kiviremoteserver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.wezom.kiviremoteserver.BuildConfig;
import com.wezom.kiviremoteserver.bus.Keyboard200;
import com.wezom.kiviremoteserver.common.ImeUtils;
import com.wezom.kiviremoteserver.common.RxBus;
import com.wezom.kiviremoteserver.service.ExecutorServiceIME;


/**
 * Created by andre on 06.06.2017.
 */

public class ImeChangesBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.equals(Intent.ACTION_INPUT_METHOD_CHANGED, action)) {
            //final String LATIN = "com.android.inputmethod.latin/.LatinIME";

            String targetImeId = BuildConfig.APPLICATION_ID + "/.service." + ExecutorServiceIME.class.getSimpleName();
            if (TextUtils.equals(targetImeId, ImeUtils.getCurrentImeKeyboardInfo(context))) {
                // todo add clause in Broadcast
                RxBus.INSTANCE.publish(new Keyboard200());
            }
        }
    }
}
