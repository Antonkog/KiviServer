package com.wezom.kiviremoteserver.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.nsd.NsdManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;
import com.wezom.kiviremoteserver.App;
import com.wezom.kiviremoteserver.R;
import com.wezom.kiviremoteserver.bus.HideKeyboardEvent;
import com.wezom.kiviremoteserver.bus.Keyboard200;
import com.wezom.kiviremoteserver.bus.NewDataEvent;
import com.wezom.kiviremoteserver.bus.NewMessageEvent;
import com.wezom.kiviremoteserver.bus.PingEvent;
import com.wezom.kiviremoteserver.bus.SendAppsListEvent;
import com.wezom.kiviremoteserver.bus.SendInitVolumeEvent;
import com.wezom.kiviremoteserver.bus.SendToSettingsEvent;
import com.wezom.kiviremoteserver.bus.SendVolumeEvent;
import com.wezom.kiviremoteserver.bus.ShowKeyboardEvent;
import com.wezom.kiviremoteserver.bus.StopReceivingEvent;
import com.wezom.kiviremoteserver.common.DeviceUtils;
import com.wezom.kiviremoteserver.common.ImeUtils;
import com.wezom.kiviremoteserver.common.KiviProtocolStructure;
import com.wezom.kiviremoteserver.common.RxBus;
import com.wezom.kiviremoteserver.common.Utils;
import com.wezom.kiviremoteserver.interfaces.DataStructure;
import com.wezom.kiviremoteserver.interfaces.RemoteServer;
import com.wezom.kiviremoteserver.mvp.view.ServiceMvpView;
import com.wezom.kiviremoteserver.net.nsd.NsdRegistrator;
import com.wezom.kiviremoteserver.net.server.KiviServer;
import com.wezom.kiviremoteserver.net.server.model.ServerApplicationInfo;
import com.wezom.kiviremoteserver.service.protocol.ServerEventStructure;
import com.wezom.kiviremoteserver.ui.activity.HomeActivity;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

import static com.wezom.kiviremoteserver.common.KiviProtocolStructure.ExecActionEnum.OPEN_SETTINGS;

/**
 * Created by andre on 02.06.2017.
 */

public class KiviRemoteService extends Service implements ServiceMvpView {

    @Inject
    NsdRegistrator nsdRegistrator;

    private final static int SERVER_ID = 123;
    public static boolean isStarted = false;

    private RemoteServer server;

    private NsdManager nsdManager;
    private Handler handler = new Handler();

    private String messIp = "";
    private final ServiceBinder binder;

    private List<ServerApplicationInfo> appList;

    private CompositeDisposable disposables;
    private AudioManager audioManager;

    private final Gson gson;

    private final RxBus bus = RxBus.INSTANCE;

    public KiviRemoteService() {
        App.getApplicationComponent().inject(this);

        binder = new ServiceBinder();
        gson = new Gson();
    }


    //endregion

    //region Override methods
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        dispose();
        disposables = new CompositeDisposable();
        nsdManager = (NsdManager) getApplicationContext().getSystemService(NSD_SERVICE);
        server = startServer();

        isStarted = true;
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        initObservers();
    }

    private void initObservers() {
        disposables.add(bus.listen(SendToSettingsEvent.class)
                .subscribe(event -> openSettings(), Timber::e));

        disposables.add(bus
                .listen(SendInitVolumeEvent.class)
                .subscribe(event -> sendVolume(), Timber::e));

        disposables.add(bus
                .listen(NewMessageEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .map(event -> {
                    Timber.d("Handle message " + event.getMessage());
                    return gson.fromJson(event.getMessage(), DataStructure.class);
                }).subscribe(request -> {
                    if (request.getAction() != null && request.getAction() == OPEN_SETTINGS) {
                        openSettings();
                        return;
                    }

                    if (handleLegacySettingsRequest(request)) return;

                    if (ImeUtils.isCurrentImeOk(this))
                        RxBus.INSTANCE.publish(new NewDataEvent(request));
                    else {
                        server.postMessage(
                                new ServerEventStructure(
                                        KiviProtocolStructure.ServerEventType.KEYBOARD_NOT_SET));
                    }
                }, Timber::e));

        disposables.add(bus.listen(Keyboard200.class).subscribe(
                event -> server.postMessage(
                        new ServerEventStructure(
                                KiviProtocolStructure.ServerEventType.KEYBOARD_200)),
                Timber::e
        ));

        disposables.add(bus.listen(ShowKeyboardEvent.class).subscribe(
                event -> server.postMessage(
                        new ServerEventStructure(
                                KiviProtocolStructure.ServerEventType.SHOW_KEYBOARD)),
                Timber::e));

        disposables.add(bus.listen(HideKeyboardEvent.class).subscribe(
                event -> server.postMessage(
                        new ServerEventStructure(
                                KiviProtocolStructure.ServerEventType.HIDE_KEYBOARD)),
                Timber::e
        ));

        disposables.add(bus.listen(StopReceivingEvent.class).subscribe(
                event -> server.stopReceiving(), Timber::e
        ));

        disposables.add(bus.listen(SendAppsListEvent.class).subscribe(
                event -> {
                    if (appList == null || appList.isEmpty()) {
                        appList = new ArrayList<>();

                        for (ApplicationInfo appInfo : event.getAppInfoList()) {
                            Drawable icon = null;
                            try {
                                icon = getPackageManager().getApplicationIcon(appInfo.packageName);
                            } catch (PackageManager.NameNotFoundException e) {
                                Timber.e(e, e.getMessage());
                            }

                            byte[] iconBytes = new byte[]{};

                            if (icon != null) {
                                Bitmap bitmap = DeviceUtils.drawableToBitmap(icon);
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 60, stream);
                                iconBytes = stream.toByteArray();
                            }

                            appList.add(new ServerApplicationInfo()
                                    .setApplicationName(DeviceUtils.getApplicationName(getPackageManager(), appInfo))
                                    .setApplicationPackage(appInfo.packageName)
                                    .setApplicationIcon(iconBytes)
                            );
                        }
                    }

                    sendBySocket(new ServerEventStructure(appList));
                }, Timber::e
        ));

        disposables.add(bus.listen(SendVolumeEvent.class).subscribe(event ->
                        server.postMessage(new ServerEventStructure(
                                KiviProtocolStructure.ServerEventType.VOLUME, event.getVolumeLevel())),
                Timber::e));

        disposables.add(bus.listen(PingEvent.class).subscribe(event -> server.sendPong(), Timber::e));
    }

    /**
     * Remove this when ios version hits 1.6 and android(remote) version hits 1.1.1
     *
     * @param request legacy open settings request
     * @return whether request was handled or not
     */
    @Deprecated
    private boolean handleLegacySettingsRequest(DataStructure request) {
        if (request.getInternal() != null && request.getInternal().equals("OPEN_SETTINGS")) {
            openSettings();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        unregisterNsd();
        server.postMessage(new ServerEventStructure(KiviProtocolStructure.ServerEventType.DISCONNECT));
        isStarted = false;
        handler.removeCallbacksAndMessages(null);
        server.disposeResources();
        dispose();
        Log.d("Log_ STOP ", "serverService stopped!!!");
        super.onDestroy();
    }

    private void dispose() {
        if (disposables != null && !disposables.isDisposed())
            disposables.dispose();
    }

    private void sendVolume() {
        Completable.timer(500, TimeUnit.MILLISECONDS).subscribe(
                () -> {
                    int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    boolean isMuted = Utils.getMuteStatus(audioManager);
                    Timber.d("Is muted: " + isMuted);
                    if (currentVolume == 0) {
                        RxBus.INSTANCE.publish(new SendVolumeEvent(0));
                        return;
                    }

                    if (isMuted) {
                        RxBus.INSTANCE.publish(new SendVolumeEvent(0));
                    } else {
                        RxBus.INSTANCE.publish(new SendVolumeEvent(1));
                    }
                }, Timber::e
        );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.postDelayed(() -> {
            Pair<String, String> address = server.getLocalIpPair(getApplicationContext());
            startForeground(SERVER_ID, createNotification(prepareIntent(), address));
        }, 300);

        return START_STICKY;
    }

    @Override
    public void registerNsd(int port) {
        Timber.d("Register nsd");
        nsdRegistrator.registerServiceNsd(port, nsdManager);
    }

    @Override
    public void unregisterNsd() {
        if (nsdRegistrator != null && nsdManager != null)
            nsdRegistrator.unregisterNsd(nsdManager);
    }

    @Override
    public void sendBySocket(ServerEventStructure structure) {
        server.postMessage(structure);
    }
    //endregion

    public static void launch(Context context) {
        Intent launcher = new Intent(context, KiviRemoteService.class);
        context.startService(launcher);
    }

    public static void stop(Context context) {
        Intent launcher = new Intent(context, KiviRemoteService.class);
        context.stopService(launcher);
    }

//    public void updateNotification() {
//        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        mNotificationManager.notify(SERVER_ID, createNotification(prepareIntent(), server.getLocalIpPair(this)));
//    }

    public class ServiceBinder extends Binder {
        public String getIpAddress() {
            return messIp;
        }
    }

    private void openSettings() {
        Observable.just("")
                .debounce(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(t ->
                        startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
    }

    private KiviServer startServer() {
        KiviServer kiviServer = new KiviServer(handler, this, this);
        kiviServer.launchServer();
        return kiviServer;
    }

    private Notification createNotification(PendingIntent pending, Pair<String, String> address) {
        String messageTitle = getString(R.string.mess_server_at, address.first, address.second);
        messIp = messageTitle;

        return new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText((messageTitle))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                .setContentIntent(pending)
                .setOngoing(true)
                .build();
    }

    private PendingIntent prepareIntent() {
        Intent nextIntent = new Intent(this, HomeActivity.class);
        nextIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(this, SERVER_ID, nextIntent, 0);
    }
}
