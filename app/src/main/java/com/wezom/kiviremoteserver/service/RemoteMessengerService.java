package com.wezom.kiviremoteserver.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;
import com.wezom.kiviremoteserver.bus.RemotePlayerEvent;
import com.wezom.kiviremoteserver.bus.TvPlayerEvent;
import com.wezom.kiviremoteserver.common.RxBus;

import java.util.ArrayList;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

/***
 * see link: https://developer.android.com/guide/components/bound-services#Messenger
 */
public class RemoteMessengerService extends Service {


    public static boolean isStarted = false;


    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;

    public static final int MSG_TV_PLAYER = 11;
    public static final int PLAY = 15;
    public static final int PAUSE = 16;
    public static final int SEEK_TO = 17;
    public static final int CLOSE = 18;
    public static final int REQUEST_STATE = 19;
    public static final int REQUEST_CONTENT = 20;

    public static final String TV_PLAYER_EVENT_KEY = "TvPlayerEvent";

    private CompositeDisposable disposables;
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    private Messenger mMessenger;

    /**
     * Keeps track of all current registered clients.
     */
    public static ArrayList<Messenger> mClients = new ArrayList<>();

    /**
     * Handler of incoming messages from clients.
     */
    static class IncomingHandler extends Handler {
        RemotePlayerEvent remotePlayerEvent;


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case PLAY:
                case SEEK_TO:
                case CLOSE:
                case PAUSE:
                case REQUEST_CONTENT:
                case REQUEST_STATE:
                    remotePlayerEvent = new RemotePlayerEvent(msg.what);
                    if (remotePlayerEvent != null) {
                        RxBus.INSTANCE.publish(remotePlayerEvent);
                    } else {
                        Timber.e("321 RemotePlayerEvent is null");
                    }
                    break;
                default:
                    Timber.e("321 got some msg " + msg.toString());
                    super.handleMessage(msg);
            }
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
//        App.getApplicationComponent().inject(this);
        dispose();
        disposables = new CompositeDisposable();
        initObservers();
        isStarted = true;
        Timber.d("create RemoteMessengerService for AIDL");
    }

    @Override
    public void onDestroy() {
        isStarted = false;
        Log.d("Log_ STOP ", "RemoteMessengerService stopped!!!");
        super.onDestroy();
    }

    private void dispose() {
        if (disposables != null && !disposables.isDisposed())
            disposables.dispose();
    }

    public static void launch(Context context) {
        Intent launcher = new Intent(context, RemoteMessengerService.class);
        context.startService(launcher);
    }

    public static void stop(Context context) {
        Intent launcher = new Intent(context, RemoteMessengerService.class);
        context.stopService(launcher);
    }


    private void initObservers() {
        RxBus.INSTANCE.listen(TvPlayerEvent.class).subscribe(event -> {
            Timber.e("321  RxBusTvPlayerEvent " + event.toString() + "  " + Looper.getMainLooper().getThread().toString());
            if (event.getPlayerAction() != null)
                for (int i = mClients.size() - 1; i >= 0; i--) {
                    try {
                        Message msg = Message.obtain(null,
                                RemoteMessengerService.MSG_TV_PLAYER);

                        Bundle data = new Bundle();
                        String extra = new Gson().toJson(event);
                        data.putString(TV_PLAYER_EVENT_KEY, extra);
                        msg.setData(data);
                        mClients.get(i).send(msg);

                    } catch (RemoteException e) {
                        // The client is dead.  Remove it from the list;
                        // we are going through the list from back to front
                        // so this is safe to do inside the loop.
                        mClients.remove(i);
                    }
                }

        }, Timber::e);

    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        mMessenger = new Messenger(new IncomingHandler());
        return mMessenger.getBinder();
    }
}
