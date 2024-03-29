package com.wezom.kiviremoteserver.service.aspect;

import android.app.Instrumentation;
import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;

public class Alarm extends IntentService {


    public Alarm() {
        super("Alarm");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.e("alarm","alarm");
        Instrumentation inst = new Instrumentation();
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_POWER);
    }
}
