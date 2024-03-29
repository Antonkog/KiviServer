package com.wezom.kiviremoteserver.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
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
import com.wezom.kiviremoteserver.bus.SendAspectEvent;
import com.wezom.kiviremoteserver.bus.SendChannelsEvent;
import com.wezom.kiviremoteserver.bus.SendFavouritesEvent;
import com.wezom.kiviremoteserver.bus.SendInitialEvent;
import com.wezom.kiviremoteserver.bus.SendInputsEvent;
import com.wezom.kiviremoteserver.bus.SendRecommendationsEvent;
import com.wezom.kiviremoteserver.bus.SendToSettingsEvent;
import com.wezom.kiviremoteserver.bus.SendVolumeEvent;
import com.wezom.kiviremoteserver.bus.ShowHideAspectEvent;
import com.wezom.kiviremoteserver.bus.ShowKeyboardEvent;
import com.wezom.kiviremoteserver.bus.SocketAcceptedEvent;
import com.wezom.kiviremoteserver.bus.StopReceivingEvent;
import com.wezom.kiviremoteserver.common.Constants;
import com.wezom.kiviremoteserver.common.DeviceUtils;
import com.wezom.kiviremoteserver.common.ImeUtils;
import com.wezom.kiviremoteserver.common.KiviProtocolStructure;
import com.wezom.kiviremoteserver.common.RxBus;
import com.wezom.kiviremoteserver.common.Utils;
import com.wezom.kiviremoteserver.environment.EnvironmentInputsHelper;
import com.wezom.kiviremoteserver.environment.EnvironmentPictureSettings;
import com.wezom.kiviremoteserver.interfaces.AspectAvailable;
import com.wezom.kiviremoteserver.interfaces.AspectMessage;
import com.wezom.kiviremoteserver.interfaces.DataStructure;
import com.wezom.kiviremoteserver.interfaces.InitialMessage;
import com.wezom.kiviremoteserver.interfaces.RemoteServer;
import com.wezom.kiviremoteserver.mvp.view.ServiceMvpView;
import com.wezom.kiviremoteserver.net.nsd.NsdUtil;
import com.wezom.kiviremoteserver.net.server.KiviServer;
import com.wezom.kiviremoteserver.net.server.model.LauncherBasedData;
import com.wezom.kiviremoteserver.receiver.AppsChangeReceiver;
import com.wezom.kiviremoteserver.receiver.ScreenOnReceiver;
import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper;
import com.wezom.kiviremoteserver.service.protocol.ServerEventStructure;
import com.wezom.kiviremoteserver.ui.activity.HomeActivity;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.wezom.kiviremoteserver.common.KiviProtocolStructure.ExecActionEnum.OPEN_SETTINGS;

/**
 * Created by andre on 02.06.2017.
 */

public class KiviRemoteService extends Service implements ServiceMvpView {

    @Inject
    NsdUtil autoDiscoveryUtil;


    private EnvironmentInputsHelper inputsHelper = null;
    private InputSourceHelper inputSourceHelper = null;
    private EnvironmentPictureSettings pictureSettings = null;


    private final static int SERVER_ID = 123;
    public static boolean isStarted = false;

    private RemoteServer server;

    private Handler handler = new Handler();
    private BroadcastReceiver screenOnReceiver = new ScreenOnReceiver();
    private BroadcastReceiver appsChangeReveiver = new AppsChangeReceiver();

    private String messIp = "";
    private final ServiceBinder binder;

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
        server = startServer();

        isStarted = true;
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        receiveScreenOn();
        receiveAppsChange();
        initObservers();
        preparePreviewCommonStructure();
    }

    private void preparePreviewCommonStructure() {
        DeviceUtils.getPreviewCommonStructureSingle(getApplicationContext()).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(previewCommonStructures ->
                        Timber.d("prepare CommonStructureSingle complete size" + previewCommonStructures.size()), e -> Timber.e(e, e.getMessage()));
    }

    private void receiveAppsChange() {
        IntentFilter appsFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        appsFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        appsFilter.addDataScheme("package");
        registerReceiver(appsChangeReveiver, appsFilter);
    }

    private void receiveScreenOn() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenOnReceiver, filter);
    }

    private void initObservers() {
        disposables.add(bus.listen(SendToSettingsEvent.class)
                .subscribe(event -> openSettings(), Timber::e));

        disposables.add(bus
                .listen(SocketAcceptedEvent.class)
                .subscribe(event -> sendVolume(), Timber::e));

        disposables.add(bus.listen(SendAspectEvent.class).subscribe(
                sendAspectEvent -> server.sendAspect(prepareAspect(), AspectAvailable.getInstance()),
                Timber::e));

        disposables.add(bus.listen(ShowHideAspectEvent.class).subscribe(
                event -> {
                    Context ctx = getApplicationContext();
                    if (Utils.isServiceRunning(AspectLayoutService.class, ctx)) {
                        ctx.stopService(new Intent(ctx, AspectLayoutService.class));
                    } else {
                        startService(new Intent(ctx, AspectLayoutService.class));
                    }
                }
                , Timber::e));

        disposables.add(bus.listen(SendInitialEvent.class).subscribe(
                sendInitialEvent -> {
                    if (sendInitialEvent.getInitialMessage() != null) {
                        server.sendInitialMsg(prepareAspect(), AspectAvailable.getInstance(), InitialMessage.getInstance());
                    }
                    if (sendInitialEvent.getStructures() != null) {
                        server.postMessage(new ServerEventStructure(KiviProtocolStructure.ServerEventType.INITIAL_II).
                                addPreviewCommonStructures(DeviceUtils.getPreviewCommonStructure(getApplicationContext())));
                    }
                },
                Timber::e));

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
                    if (request.getAspectMessage() != null) {
                        try {
                            syncAspectWithPhone(request.getAspectMessage());
                        } catch (Exception e) {
                            Timber.e("error while syncing aspect with phone " + e.getMessage());
                        }
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

        disposables.add(bus.listen(SendInputsEvent.class).subscribe(event -> server.sendInputs(InputSourceHelper.getAsInputs(getApplicationContext())), Timber::e));

        disposables.add(bus.listen(SendRecommendationsEvent.class).subscribe(event -> server.sendRecommendations(DeviceUtils.getLauncherData(null, LauncherBasedData.TYPE.RECOMMENDATION, getApplicationContext())), Timber::e));

        disposables.add(bus.listen(SendChannelsEvent.class).subscribe(event -> server.sendChannels(DeviceUtils.getLauncherData(null, LauncherBasedData.TYPE.CHANNEL, getApplicationContext())), Timber::e));

        disposables.add(bus.listen(SendFavouritesEvent.class).subscribe(event -> server.sendFavourites(DeviceUtils.getLauncherData(null, LauncherBasedData.TYPE.FAVOURITE, getApplicationContext())), Timber::e));


        disposables.add(bus.listen(SendVolumeEvent.class).subscribe(event ->
                        server.postMessage(new ServerEventStructure(
                                KiviProtocolStructure.ServerEventType.VOLUME, event.getVolumeLevel())),
                Timber::e));

        disposables.add(bus.listen(PingEvent.class).subscribe(event -> server.sendPong(), Timber::e));

        disposables.add(bus.listen(SendAppsListEvent.class).subscribe(
                event -> {
                    sendBySocket(new ServerEventStructure(KiviProtocolStructure.ServerEventType.APPS)
                            .addApps(event.getServerApplicationInfos()));
                }, Timber::e
        ));

    }


    private void syncAspectWithPhone(AspectMessage message) {
        if (message.settings != null) {
            Iterator it = message.settings.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> pair = (Map.Entry) it.next();

                if (AspectMessage.ASPECT_VALUE.valueOf(pair.getKey()) == AspectMessage.ASPECT_VALUE.INPUT_PORT) {
                    if (message.settings.size() == 1 //todo: for remote verion 1.1.14  remove later only this row
                            && pair.getValue() != Constants.NO_VALUE) {
                        if (inputSourceHelper == null)
                            inputSourceHelper = new InputSourceHelper();
                        inputSourceHelper.changeInput(pair.getValue(), getApplicationContext());
                    }
                } else {
                    if (pictureSettings == null)
                        pictureSettings = new EnvironmentPictureSettings();
                    if (pictureSettings.isSafe())
                        switch (AspectMessage.ASPECT_VALUE.valueOf(pair.getKey())) {
                            case PICTUREMODE:
                                pictureSettings.setPictureMode(pair.getValue());
                                break;
                            case BRIGHTNESS:
                                pictureSettings.setBrightness(pair.getValue());
                                break;
                            case SHARPNESS:
                                pictureSettings.setSharpness(pair.getValue());
                                break;
                            case SATURATION:
                                pictureSettings.setSaturation(pair.getValue());
                                break;
                            case BACKLIGHT:
                                pictureSettings.setBacklight(pair.getValue(), getBaseContext());
                                break;
                            case GREEN:
                                pictureSettings.setGreen(pair.getValue());
                                break;
                            case RED:
                                pictureSettings.setRed(pair.getValue());
                                break;
                            case BLUE:
                                pictureSettings.setBLue(pair.getValue());
                                break;
                            case HDR:
                                pictureSettings.setHDR(pair.getValue());
                                break;
                            case TEMPERATURE:
                                pictureSettings.setTemperature(pair.getValue());
                                break;
                            case CONTRAST:
                                pictureSettings.setContrast(pair.getValue());
                                break;
                            case VIDEOARCTYPE:
                                pictureSettings.setVideoArcType(pair.getValue());
                                break;
                            case SERVER_VERSION_CODE:
                                break;
                            default:
                                Timber.e("wrong aspect value");
                        }
                }
                it.remove(); // avoids a ConcurrentModificationException
            }
        }
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
        unregisterReceiver(screenOnReceiver);
        unregisterReceiver(appsChangeReveiver);
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
                        RxBus.INSTANCE.publish(new SendVolumeEvent(currentVolume));
                    }
                }, Timber::e
        );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.postDelayed(() -> {
            Pair<String, String> address = server.getLocalIpPair(getApplicationContext());
//            startForeground(SERVER_ID, createNotification(prepareIntent(), address));
        }, 300);

        return START_STICKY;
    }

    @Override
    public void registerNsd(int port) {
        Timber.d("Register nsd");
        autoDiscoveryUtil = new NsdUtil(getApplicationContext());
        autoDiscoveryUtil.initializeResolveListener();
        autoDiscoveryUtil.registerService(port);
        autoDiscoveryUtil.discoverServices();
    }

    @Override
    public void unregisterNsd() {
        autoDiscoveryUtil.tearDown();
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

    @NonNull
    private AspectMessage prepareAspect() {
        if (inputsHelper == null) inputsHelper = new EnvironmentInputsHelper();
        if (inputSourceHelper == null) inputSourceHelper = new InputSourceHelper();
        if (pictureSettings == null) pictureSettings = new EnvironmentPictureSettings();
        pictureSettings.initSettings(getApplicationContext());
        if (App.isTVRealtek()) pictureSettings.initColors(); //not working for MStar
        AspectMessage msg = new AspectMessage(pictureSettings, inputsHelper);
        AspectAvailable.getInstance().setValues(getApplicationContext(), inputSourceHelper, inputsHelper);
        return msg;
    }


    private PendingIntent prepareIntent() {
        Intent nextIntent = new Intent(this, HomeActivity.class);
        nextIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(this, SERVER_ID, nextIntent, 0);
    }
}
