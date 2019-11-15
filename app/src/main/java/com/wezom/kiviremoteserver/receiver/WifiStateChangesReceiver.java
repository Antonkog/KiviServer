package com.wezom.kiviremoteserver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.wezom.kiviremoteserver.App;
import com.wezom.kiviremoteserver.bus.NetworkStateEvent;
import com.wezom.kiviremoteserver.common.RxBus;
import com.wezom.kiviremoteserver.service.RemoteReceiverService;
import com.wezom.kiviremoteserver.service.RemoteSenderService;

import timber.log.Timber;

/**
 * Created by andre on 06.06.2017.
 */

public class WifiStateChangesReceiver extends BroadcastReceiver {
    private ConnectionState lastState = ConnectionState.UNKNOWN;

    @Override
    public void onReceive(Context context, Intent intent) {
        App.getApplicationComponent().inject(this);
        debugIntent(intent);
        reactOnChanges(context, intent);
    }

    private void reactOnChanges(Context context, Intent intent) {
        NetworkInfo netInfo = (NetworkInfo) intent.getExtras().get("networkInfo");
        if (netInfo == null) {
            return;
        }

        boolean connected = netInfo.getState() == NetworkInfo.State.CONNECTED || netInfo.getState() == NetworkInfo.State.CONNECTING;
        boolean disconnected = netInfo.getState() == NetworkInfo.State.DISCONNECTED || netInfo.getState() == NetworkInfo.State.DISCONNECTING;

        ConnectionState currState;
        if (connected) {
            currState = ConnectionState.CONNECTED;
            RxBus.INSTANCE.publish(new NetworkStateEvent(true));
        } else if (disconnected) {
            currState = ConnectionState.DISCONNECTED;
            RxBus.INSTANCE.publish(new NetworkStateEvent(false));
        } else {
            RxBus.INSTANCE.publish(new NetworkStateEvent(false));
            currState = ConnectionState.UNKNOWN;
        }
        // todo cleanup, in Manifest too
//    if (intent?.getBoolanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false) ?: false) {
//    if (context.isConnectedViaWiFi()) {
        if (currState == lastState) {
            return;
        }
        if (currState == ConnectionState.CONNECTED) {
            startServer(context);
        } else if (currState == ConnectionState.DISCONNECTED) {
            Timber.d("Disconnected from WIFI. Kill server");
            RemoteSenderService.stop(context);
            RemoteReceiverService.stop(context);
        } else { /* fixme network state undefiined. Fix*/
        }

        lastState = currState;
    }

    private void startServer(Context context) {
//        Timber.d("Connected to WIFI. Start server ");
//        if (KiviRemoteService.isStarted) {
//            KiviRemoteService.stop(context);
//        }
        RemoteSenderService.launch(context);
        RemoteReceiverService.launch(context);
    }

    private void debugIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        Timber.d("action: " + intent.getAction());
        Timber.d("component: " + intent.getComponent());
        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Timber.d("key [" + key + "]: " +
                        extras.get(key));
            }
        } else {
            Timber.d("no extras");
        }
    }

    private enum ConnectionState {
        CONNECTED, DISCONNECTED, UNKNOWN
    }
}
