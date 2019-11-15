package com.wezom.kiviremoteserver.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.kivi.launcher_v2.IPlayerControl;
import com.wezom.kiviremoteserver.App;
import com.wezom.kiviremoteserver.IPlayerListener;
import com.wezom.kiviremoteserver.bus.RemotePlayerEvent;
import com.wezom.kiviremoteserver.bus.TvPlayerEvent;
import com.wezom.kiviremoteserver.common.KiviProtocolStructure;
import com.wezom.kiviremoteserver.common.RxBus;
import com.wezom.kiviremoteserver.net.server.model.LauncherBasedData;
import com.wezom.kiviremoteserver.net.server.model.PreviewCommonStructure;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

import static com.wezom.kiviremoteserver.service.RemoteMessengerService.CLOSE;
import static com.wezom.kiviremoteserver.service.RemoteMessengerService.PAUSE;
import static com.wezom.kiviremoteserver.service.RemoteMessengerService.PLAY;
import static com.wezom.kiviremoteserver.service.RemoteMessengerService.REQUEST_CONTENT;
import static com.wezom.kiviremoteserver.service.RemoteMessengerService.REQUEST_STATE;
import static com.wezom.kiviremoteserver.service.RemoteMessengerService.SEEK_TO;

public class AidlPlayerService extends Service {

    private CompositeDisposable disposables;
    private IPlayerControl playerControl;
    private boolean serviceConnectionBound = false;

    Messenger messenger = null;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Timber.e("AidlPlayerService connected");
            IPlayerControl iPlayerControl = IPlayerControl.Stub.asInterface(service);
            if (iPlayerControl != null) {
                playerControl = iPlayerControl;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playerControl = null;
            Timber.e("AidlPlayerService has unexpectedly disconnected");
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (playerControl == null) {
            connectToPlayerControl();
        }
        return new IPlayerListener.Stub() {

            @Override
            public void launchPlayer(int contentId, int parentContentId, String title, String description, String imageUrl, int duration) throws RemoteException {
                HashMap<String, String> stringStringHashMap = new HashMap<>();
                stringStringHashMap.put("parentContentId", "" + parentContentId);
                stringStringHashMap.put("duration", "" + duration);
                PreviewCommonStructure ps = new PreviewCommonStructure(LauncherBasedData.TYPE.TV_PREVIEW.name(), "" + contentId, title, imageUrl, true, stringStringHashMap);
                Timber.e("AidlPlayerService launchPlayer");
                sendTvPlayerEvent(KiviProtocolStructure.ServerEventType.LAUNCH_PLAYER, contentId, ps);
                //                    sendTvPlayerEvent(new TvPlayerEvent(KiviProtocolStructure.ServerEventType.LAUNCH_PLAYER, null, ps));


            }
            @Override
            public void changeState(int newState) throws RemoteException {
                Timber.e("AidlPlayerService changeState");
                sendTvPlayerEvent(KiviProtocolStructure.ServerEventType.CHANGE_STATE, newState, null);
            }

            @Override
            public void seekTo(int progressPercent) throws RemoteException {
                sendTvPlayerEvent(KiviProtocolStructure.ServerEventType.SEEK_TO, progressPercent, null);
            }
        };
    }

    private void sendTvPlayerEvent(KiviProtocolStructure.ServerEventType actionType, int volume, @Nullable PreviewCommonStructure previewCommonStructure) {
        Timber.e("AidlPlayerService sending action to remote " + actionType.name());
        RxBus.INSTANCE.publish(new TvPlayerEvent(actionType, volume, previewCommonStructure));
    }

    boolean isRemoteConnected() {
        return true;
    }

    private void connectToPlayerControl() {
        Intent intent = new Intent();
        intent.setAction("com.kivi.launcher_v2.IPlayerControl");
        intent.setComponent(new ComponentName("com.kivi.launcher_v2",
                "com.kivi.launcher_v2.services.AidlPlayerControlService"));

        serviceConnectionBound = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.e(this.getClass().getSimpleName(), "serviceConnection" + (serviceConnectionBound ? "serviceConnectionBound" : "not serviceConnectionBound"));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        App.getApplicationComponent().inject(this);
        dispose();
        disposables = new CompositeDisposable();
        messenger = new Messenger(new RemoteMessengerService.IncomingHandler());
        Timber.d("create IME_Service");
        initObservers();
    }

    @Override
    public void onDestroy() {
        if (serviceConnectionBound) {
            unbindService(serviceConnection);
            serviceConnectionBound = false;
        }
        Log.d("Log_ STOP ", "serverService stopped!!!");
        dispose();
        super.onDestroy();
    }


    private void initObservers() {
        disposables.add(RxBus.INSTANCE
                .listen(RemotePlayerEvent.class)
                .subscribe(this::handleRequest, Timber::e));
    }

    public void handleRequest(RemotePlayerEvent playerEvent) {
        Timber.e("got  from phone " + playerEvent.toString());
        if (playerControl == null)
            sendTvPlayerEvent(KiviProtocolStructure.ServerEventType.LAST_REQUEST_ERROR, 0, null);
        else
            try {
                switch (playerEvent.getNum()) {
                    case PLAY:
                        playerControl.play();
                        break;
                    case CLOSE:
                        playerControl.close();
                        break;
                    case PAUSE:
                        playerControl.pause();
                        break;
                    case REQUEST_STATE:
                        playerControl.reloadState();
                        break;
                    case SEEK_TO:
                        playerControl.seekTo(playerEvent.getProgress());
                        break;
                    case REQUEST_CONTENT:
                        playerControl.requeestConetentInfo();
                        break;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
    }

    private void dispose() {
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
    }
}