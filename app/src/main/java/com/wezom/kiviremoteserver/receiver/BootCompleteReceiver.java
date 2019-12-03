package com.wezom.kiviremoteserver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wezom.kiviremoteserver.service.CursorService;
import com.wezom.kiviremoteserver.service.RemoteConlrolService;

/**
 * Created by andre on 06.06.2017.
 */

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            context.startService(new Intent(context, CursorService.class));
            context.startService(new Intent(context, RemoteConlrolService.class));
        }
    }
}
