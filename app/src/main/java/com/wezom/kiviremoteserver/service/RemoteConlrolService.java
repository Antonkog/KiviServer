package com.wezom.kiviremoteserver.service;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.google.gson.Gson;
import com.wezom.kiviremoteserver.App;
import com.wezom.kiviremoteserver.R;
import com.wezom.kiviremoteserver.bus.HideKeyboardEvent;
import com.wezom.kiviremoteserver.bus.Keyboard200;
import com.wezom.kiviremoteserver.bus.NewMessageEvent;
import com.wezom.kiviremoteserver.bus.PingEvent;
import com.wezom.kiviremoteserver.bus.SendAppsListEvent;
import com.wezom.kiviremoteserver.bus.SendAspectEvent;
import com.wezom.kiviremoteserver.bus.SendImgByIds;
import com.wezom.kiviremoteserver.bus.SendInitialEvent;
import com.wezom.kiviremoteserver.bus.SendToSettingsEvent;
import com.wezom.kiviremoteserver.bus.SendVolumeEvent;
import com.wezom.kiviremoteserver.bus.ShowHideAspectEvent;
import com.wezom.kiviremoteserver.bus.ShowKeyboardEvent;
import com.wezom.kiviremoteserver.bus.SocketAcceptedEvent;
import com.wezom.kiviremoteserver.bus.StopReceivingEvent;
import com.wezom.kiviremoteserver.bus.ToKeyboardExecutorEvent;
import com.wezom.kiviremoteserver.bus.TvPlayerEvent;
import com.wezom.kiviremoteserver.bus.VolumeEvent;
import com.wezom.kiviremoteserver.common.AppsInfoLoader;
import com.wezom.kiviremoteserver.common.Constants;
import com.wezom.kiviremoteserver.common.DeviceUtils;
import com.wezom.kiviremoteserver.common.ImeUtils;
import com.wezom.kiviremoteserver.common.KiviProtocolStructure;
import com.wezom.kiviremoteserver.common.MotionRelay;
import com.wezom.kiviremoteserver.common.RxBus;
import com.wezom.kiviremoteserver.common.Utils;
import com.wezom.kiviremoteserver.environment.EnvironmentInputsHelper;
import com.wezom.kiviremoteserver.environment.EnvironmentPictureSettings;
import com.wezom.kiviremoteserver.interfaces.AspectAvailable;
import com.wezom.kiviremoteserver.interfaces.AspectMessage;
import com.wezom.kiviremoteserver.interfaces.DataStructure;
import com.wezom.kiviremoteserver.interfaces.RemoteServer;
import com.wezom.kiviremoteserver.mvp.view.ServiceMvpView;
import com.wezom.kiviremoteserver.net.nsd.NsdUtil;
import com.wezom.kiviremoteserver.net.server.KiviServer;
import com.wezom.kiviremoteserver.net.server.model.LauncherBasedData;
import com.wezom.kiviremoteserver.net.server.model.LongTapAction;
import com.wezom.kiviremoteserver.net.server.model.PreviewCommonStructure;
import com.wezom.kiviremoteserver.receiver.AppsChangeReceiver;
import com.wezom.kiviremoteserver.receiver.ScreenOnReceiver;
import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper;
import com.wezom.kiviremoteserver.service.protocol.ServerEventStructure;
import com.wezom.kiviremoteserver.ui.activity.HomeActivity;

import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.wezom.kiviremoteserver.common.Constants.LAST_VOLUME;
import static com.wezom.kiviremoteserver.common.KiviProtocolStructure.ExecActionEnum.OPEN_SETTINGS;
import static com.wezom.kiviremoteserver.net.nsd.NsdUtil.DEVICE_NAME_KEY;

/**
 * Created by andre on 02.06.2017.
 */

public class RemoteConlrolService extends Service implements ServiceMvpView {

    @Inject
    NsdUtil autoDiscoveryUtil;

    @Inject
    DeviceUtils deviceUtils;

    @Inject
    AppsInfoLoader appsInfoLoader;

    @Inject
    Instrumentation instrumentation;

    @Inject
    InputSourceHelper inputSourceHelper;

    @Inject
    AudioManager audioManager;


    CompositeDisposable disposables;

    public static boolean isStarted = false;

    private SharedPreferences prefs;

    private final int width = Resources.getSystem().getDisplayMetrics().widthPixels;
    private final int height = Resources.getSystem().getDisplayMetrics().heightPixels;
    private final int x = width / 2;
    private final int y = height / 2;
    private final int DEFAULT_PREF_VOLUME = -2;

    private long scrollTime = System.currentTimeMillis();

    private Disposable disposableInit_II;
    private Disposable disposableInit;
    private Disposable scrollDisposable;
    private Disposable requestAppsDisposable;

    private EnvironmentInputsHelper inputsHelper = null;
    private EnvironmentPictureSettings pictureSettings = null;


    private final static int SERVER_ID = 123;

    private RemoteServer server;

    private Handler handler = new Handler();
    private BroadcastReceiver screenOnReceiver = new ScreenOnReceiver();
    private BroadcastReceiver appsChangeReveiver = new AppsChangeReceiver();

    private String messIp = "";
    private final ServiceBinder binder;

    private Messenger remoteMessenger = null;
    private Messenger internalMsgr = null;

    boolean remoteMsgrBound = false;

    private final Gson gson;

    private final RxBus bus = RxBus.INSTANCE;


    public RemoteConlrolService() {
        App.getApplicationComponent().inject(this);
        binder = new ServiceBinder();
        gson = new Gson();
    }

    //region Override methods
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        Timber.e("RemoteSenderService onCreate");
        dispose();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        disposables = new CompositeDisposable();
        server = startServer();

        isStarted = true;
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        handler.postDelayed(() -> {
            appsInfoLoader.init(this);
        }, 300);

        receiveScreenOn();
        receiveAppsChange();
        initObservers();
        connectToRemoteMessenger();
    }


    private KiviServer startServer() {
        KiviServer kiviServer = new KiviServer(handler, this, this);
        kiviServer.launchServer();
        return kiviServer;
    }

    private void dispose() {
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
            dispose(disposableInit);
            dispose(disposableInit_II);
            dispose(scrollDisposable);
            dispose(requestAppsDisposable);
        }
    }

    private void dispose(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    private void connectToRemoteMessenger() {
        if (!remoteMsgrBound) {
            if (bindService(new Intent(this, RemoteMessengerService.class), remoteServiceConnection,
                    Context.BIND_AUTO_CREATE)) {
                remoteMsgrBound = true;
                Timber.e(" bind RemoteMessengerService  - success");
            } else {
                Timber.e("  can't bind RemoteMessengerService");
            }
        } else {
            Timber.e("  RemoteMessengerService remoteMsgrBound");
        }
    }


    private void initObservers() {
        disposables.add(bus
                .listen(NewMessageEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .map(event -> {
                    String msg = event.getMessage();
                    Timber.d("Handle message " + msg);
                    DataStructure dataStructure = gson.fromJson(msg, DataStructure.class);
                    return dataStructure;
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

                    if (ImeUtils.isCurrentImeOk(this)) {
                        handleDataRequest(request);
                        Timber.d("posting new data event, " + request.getAction().name());
                    } else {
                        server.postMessage(
                                new ServerEventStructure(
                                        KiviProtocolStructure.ServerEventType.KEYBOARD_NOT_SET));
                    }
                }, Timber::e));


        disposables.add(RxBus.INSTANCE
                .listen(VolumeEvent.class).subscribe(volumeEvent -> {
                    if (volumeEvent.isMuteEvent()) muteWorkAround();
                    else sendVolume();
                }, Timber::e));

        disposables.add(bus.listen(SendToSettingsEvent.class)
                .subscribe(event -> openSettings(), Timber::e));

        disposables.add(bus.listen(LongTapAction.class)
                .subscribe(event -> {
//                    Timber.e("Long tap action 2 = " +event.getName());
                    switch (event.getName()){
                        case R.string.lt_back:
                            executeCommand(ToKeyboardExecutorEvent.COMMAND_NORMAL, KeyEvent.KEYCODE_BACK, null);
                            break;

                        case R.string.lt_home: //homedown then homeup
                            executeKeyDownInstrumentation(KeyEvent.KEYCODE_HOME);
                            navigateHome();
                            break;

                        case R.string.lt_q_settings:
                            RxBus.INSTANCE.publish(new ShowHideAspectEvent());
                            break;

                        case R.string.lt_settings:
                            Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                            startActivity(intent);
                            break;

                        case R.string.lt_hdmi_settings:
                            executeKeyDownInstrumentation(KeyEvent.KEYCODE_MENU);
                            break;

                        case R.string.lt_channels_list:
                            executeCommand(ToKeyboardExecutorEvent.COMMAND_NORMAL, KeyEvent.KEYCODE_DPAD_CENTER, null);
                            break;
                        case R.string.lt_film_catalogue:
                            sendBroadcast(new Intent("com.kivi.launcher_v2.ACTION_VIDEO_CATALOG"));
                            break;
                        case R.string.lt_widgets:
                            try {
                                Intent widgetIntent = new Intent();
                                widgetIntent.setComponent(new ComponentName("com.kivi.widget2", "com.kivi.widget2.MainActivity"));
                                startActivity(widgetIntent);
                            } catch (ActivityNotFoundException e){
                                Timber.e(" com.kivi.widget2 not found on tv" + e.getMessage());
                            }
                            break;
                    }
                }, Timber::e));


        disposables.add(bus
                .listen(SocketAcceptedEvent.class)
                .subscribe(event -> sendVolume(), Timber::e));

        disposables.add(bus.listen(SendAspectEvent.class).subscribe(
                sendAspectEvent -> server.sendAspect(prepareAspect(), AspectAvailable.getInstance()),
                Timber::e));


        disposables.add(bus.listen(SendImgByIds.class).subscribe(
                event -> server.postMessage(new ServerEventStructure(KiviProtocolStructure.ServerEventType.IMG_BY_IDS)
                                .addPreviewContents(appsInfoLoader.getImgByIds(event.getIds()))),
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
                    if (sendInitialEvent.getStructures() != null) { //initial 2 (2.0.+ version)
                        server.postMessage(new ServerEventStructure(KiviProtocolStructure.ServerEventType.INITIAL_II).
                                addPreviewCommonStructures(sendInitialEvent.getStructures()));
                    } else { //initial 1 (old versions)
                        server.sendInitialMsg(prepareAspect(), AspectAvailable.getInstance(), sendInitialEvent.getInitialMessage());
                    }
                },
                Timber::e));

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

    public void handleDataRequest(DataStructure dataStructure) {
        Timber.d("Received action: " + dataStructure.getAction() + " request");

        if (dataStructure.getAction() != null) {
            switch (dataStructure.getAction()) {
                case KEY_EVENT:
                case keyevent:
                    executeCommand(ToKeyboardExecutorEvent.COMMAND_NORMAL, parseIntOrLogError(dataStructure.getArgs().get(0)), null);
                    break;

                case TEXT:
                case text:
                    executeCommand(ToKeyboardExecutorEvent.TEXT, 0, dataStructure.getArgs().get(0));
                    break;

                case MOTION:
                case motion:
                    MotionRelay.INSTANCE
                            .getRelay()
                            .accept(new MotionRelay.CursorMotionEvent(MotionRelay.UPDATE_CURSOR_POSITION, dataStructure.getMotion().get(0),
                                    dataStructure.getMotion().get(1)));
                    break;

                case LEFT_CLICK:
                case leftClick:
                    executeCommand(ToKeyboardExecutorEvent.CLICK, 0, null);

                    break;

                case RIGHT_CLICK:
                case rightClick:
                    executeCommand(ToKeyboardExecutorEvent.COMMAND_NORMAL, KeyEvent.KEYCODE_BACK, null);
                    break;

                case LONG_TAP_DOWN:
                    MotionRelay.INSTANCE
                            .getRelay()
                            .accept(new MotionRelay.CursorMotionEvent(MotionRelay.LONG_TAP_DOWN, dataStructure.getMotion().get(0),
                                    dataStructure.getMotion().get(1)));
                    break;

                case LONG_TAP_UP:
                    MotionRelay.INSTANCE
                            .getRelay()
                            .accept(new MotionRelay.CursorMotionEvent(MotionRelay.LONG_TAP_UP, dataStructure.getMotion().get(0),
                                    dataStructure.getMotion().get(1)));

                    break;

                case VOICE_SEARCH:
                    Timber.e("VOICE_SEARCH" + dataStructure.getArgs().get(0));
                    break;

                case SET_VOLUME:
                    int volume = parseIntOrLogError(dataStructure.getArgs().get(0));
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
                    Timber.d("SET_VOLUME" + volume);
                    break;

                case REQUEST_APPS:
                    dispose(requestAppsDisposable);
                    requestAppsDisposable = appsInfoLoader.getAppsList()
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    apps -> RxBus.INSTANCE.publish(new SendAppsListEvent(apps)),
                                    e -> Timber.e(e, e.getMessage()));
                    break;

                case LAUNCH_APP:
                    if (dataStructure.getPackageName() != null)
                        launchApp(dataStructure.getPackageName());
                    break;

                case REQUEST_VOLUME:
                    boolean isMuted = Utils.getMuteStatus(audioManager);
                    Timber.d("Is muted: " + isMuted);
                    RxBus.INSTANCE.publish(new SendVolumeEvent(isMuted ? 0 : 1));
                    break;

                case PING:
                    RxBus.INSTANCE.publish(new PingEvent());
                    break;

                case SWITCH_OFF:
                    new Thread(() -> executeKeyDownUp(KeyEvent.KEYCODE_POWER)).start();
                    //   launchApp("com.funshion.poweroffdialog");
                    break;

                case SCROLL:
                    float dy = dataStructure.getMotion().get(1);
                    if (isBrowserCurrent()) {
                        scroll(dy);
                    } else {
                        if (System.currentTimeMillis() - scrollTime > Constants.SCROLL_VELOCITY_MS) {
                            scrollTime = System.currentTimeMillis();
                            if (dy > 0) {
                                executeCommand(ToKeyboardExecutorEvent.COMMAND_NORMAL, KeyEvent.KEYCODE_DPAD_DOWN, null);
                            } else {
                                executeCommand(ToKeyboardExecutorEvent.COMMAND_NORMAL, KeyEvent.KEYCODE_DPAD_UP, null);
                            }
                        }
                    }
                    break;
                case HOME_DOWN:
                    executeKeyDownInstrumentation(KeyEvent.KEYCODE_HOME);
                    break;
                case HOME_UP:
                    navigateHome();
                    break;
                case LAUNCH_QUICK_APPS:
                    launchQuickApps();
                    break;
                case NAME_CHANGE:
                    String name = dataStructure.getArgs().get(0);
                    if (name != null && !name.trim().isEmpty()) {
                        Settings.Global.putString(getApplicationContext().getContentResolver(), DEVICE_NAME_KEY, name);
                        Settings.System.putString(getApplicationContext().getContentResolver(), DEVICE_NAME_KEY, name);
                    }
                    break;
                case REQUEST_ASPECT:
                    RxBus.INSTANCE.publish(new SendAspectEvent());
                    break;
                case SHOW_OR_HIDE_ASPECT:
                    RxBus.INSTANCE.publish(new ShowHideAspectEvent());
                    break;
                case REQUEST_INITIAL:
                    dispose(disposableInit);
                    disposableInit = deviceUtils.getInitialSingle(getApplicationContext()).
                                    subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(initialMessage -> RxBus.INSTANCE.publish(new SendInitialEvent(initialMessage)),
                                            e -> Timber.e(e, e.getMessage()));
                    break;
                case REQUEST_INITIAL_II:
                    dispose(disposableInit_II);
                    disposableInit_II = deviceUtils.getPreviewCommonStructureSingle()
                                    .subscribeOn(Schedulers.computation())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(previewCommonStructures -> RxBus.INSTANCE.publish(new SendInitialEvent(previewCommonStructures)),
                                            e -> Timber.e(e, e.getMessage()));
                    break;
                case REQUEST_IMG_BY_IDS:
                    if (dataStructure.getArgs() != null)
                        RxBus.INSTANCE.publish(new SendImgByIds(dataStructure.getArgs()));
                    else Timber.e("asking for previews but no id");
                    break;
                case LAUNCH_CHANNEL:
                    startLauncherIntent(LauncherBasedData.TYPE.CHANNEL, dataStructure.getArgs().get(0));
                    break;
                case LAUNCH_RECOMMENDATION:
                    startLauncherIntent(LauncherBasedData.TYPE.RECOMMENDATION, dataStructure.getArgs().get(0));
                    break;
                case LAUNCH_FAVORITE:
                    startLauncherIntent(LauncherBasedData.TYPE.FAVOURITE, dataStructure.getArgs().get(0));
                    break;
                case PLAYER_ACTION:
                    Timber.e("action in RemoteReciever is  PLAYER_ACTION");
                    Timber.e(" got ExecutorPlayerEvent, " + dataStructure.getArgs().get(0));
                    connectToRemoteMessenger();

                    float progress = dataStructure.getMotion() == null ? 0 : dataStructure.getMotion().get(0);
                    sendToRemoteMessenger(dataStructure.getArgs().get(0), progress);

                    break;
                case CHANGE_INPUT:
                    inputSourceHelper.changeInput(
                            parseIntOrLogError(dataStructure.getArgs().get(0)),
                            getApplicationContext());
                    break;
                default:
                    Timber.e("some not handled action in IME => %s", dataStructure.getAction());
                    break;
            }
        } else {
            Timber.e("server got message but dataStructure.getAction() == null");
        }
    }

    private void executeCommand(int type, int keyCode, String text) {
        RxBus.INSTANCE.publish(new ToKeyboardExecutorEvent(type, keyCode, text));
    }


    private int executeKeyDownUp(int keyCode) {
        try {
            instrumentation.sendKeyDownUpSync(keyCode);
        } catch (SecurityException e) {
            Timber.e(e, "Failed to inject event: " + e.getMessage());
        }
        return keyCode;
    }

    private void muteWorkAround() {
        int oldVolume = prefs.getInt(LAST_VOLUME, DEFAULT_PREF_VOLUME);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume != 0) {
            prefs.edit().putInt(LAST_VOLUME, currentVolume).apply();
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI);
        } else {
            if (oldVolume != DEFAULT_PREF_VOLUME) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, oldVolume, AudioManager.FLAG_SHOW_UI);
            }
        }
        sendVolume();
    }

    private void startLauncherIntent(LauncherBasedData.TYPE type, String args) {
        Intent i = new Intent();
        i.setComponent(new ComponentName(Constants.LAUNCHER_PACKAGE, Constants.LAUNCHER_SERVICE));
        switch (type) {
            case CHANNEL:
                i.putExtra("type", Constants.CHANNEL_MANAGER);
                break;
            case RECOMMENDATION:
                i.putExtra("type", Constants.RECOMMENDATION_MANAGER);
                break;
            case FAVOURITE:
                i.putExtra("type", Constants.FAVORITES_MANAGER);
                break;
        }
        i.putExtra("item", args);
        startService(i);
    }

    private void launchApp(String packageName) {
        try {
            Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);
            startActivity(intent);
        } catch (Exception e) {
            Timber.e(e, e.getMessage());
        }
    }


    private boolean isBrowserCurrent() {
        ActivityManager mActivityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = "";
        if (Build.VERSION.SDK_INT > 20) {
            packageName = mActivityManager.getRunningAppProcesses().get(0).processName;
        } else {
            packageName = mActivityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
        }
        Timber.e("current package : " + packageName);
        if (packageName.contains("browser")) return true;
        return false;
    }


    private void executeKeyDownInstrumentation(int keyCode) {
        Observable
                .fromCallable(() -> keyDownInstrumentation(keyCode))
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }

    private int keyDownInstrumentation(int keyCode) {
        try {
            instrumentation.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
        } catch (SecurityException e) {
            Timber.e(e);
        }
        return 0;
    }


    private void scroll(float y) {
        dispose(scrollDisposable);
        scrollDisposable = Completable
                .fromCallable(() -> executeScroll(y))
                .subscribeOn(Schedulers.io())
                .doOnError(e -> Timber.e(e, e.getMessage()))
                .subscribe();
    }


    public int executeScroll(float dy) throws Exception {
        long start = SystemClock.uptimeMillis();
        instrumentation.sendPointerSync(MotionEvent.obtain(
                start,
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN, x, y, 0));
        start = SystemClock.uptimeMillis();
        instrumentation.sendPointerSync(MotionEvent.obtain(
                start,
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_MOVE, x, y + (-dy) * 10, 0));
        start = SystemClock.uptimeMillis();
        instrumentation.sendPointerSync(MotionEvent.obtain(
                start,
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_CANCEL, x, y, 0));
        return 0;
    }


    private void navigateHome() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void launchQuickApps() {
        Intent intent = new Intent("tv.fun.intent.action.ACTION_HOME");
        intent.setComponent(new ComponentName("com.bestv.ott", "com.bestv.ott.HomeTransferService"));
        intent.putExtra("sendFrom", 0);
        intent.putExtra("isLongPressed", true);
        startService(intent);
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

    public void sendToRemoteMessenger(String strNum, float volume) {
        if (!remoteMsgrBound || remoteMessenger == null) {
            Timber.e("123 not remoteMsgrBound or remoteMessenger==null");
            return;
        }
        try {
            int num = -1;
            switch (strNum) {
                case "PAUSE":
                    num = RemoteMessengerService.PAUSE;
                    break;
                case "PLAY":
                    num = RemoteMessengerService.PLAY;
                    break;
                case "CLOSE":
                    num = RemoteMessengerService.CLOSE;
                    break;
                case "SEEK_TO":
                    num = RemoteMessengerService.SEEK_TO;
                    break;
                case "REQUEST_CONTENT":
                    num = RemoteMessengerService.REQUEST_CONTENT;
                    break;
                case "REQUEST_STATE":
                    num = RemoteMessengerService.REQUEST_STATE;
                    break;
            }

            Bundle bundle = new Bundle();
            bundle.putFloat(RemoteMessengerService.PROGRESS_KEY, volume);
            Message msg = Message.obtain(null, num);
            msg.setData(bundle);
            remoteMessenger.send(msg);
            Timber.e("  sending to RemoteMessengerService : " + msg.toString());
        } catch (RemoteException e) {
            Timber.e("321 remoteMessenger disconnect");
        }
    }

    private ServiceConnection remoteServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            remoteMessenger = new Messenger(service);
            registerMsgListener();
            remoteMsgrBound = true;
            Timber.e(" service connected :  " + className.getClassName());
        }

        public void onServiceDisconnected(ComponentName className) {
            remoteMessenger = null;
            remoteMsgrBound = false;
            Timber.e(" service disconnected :  " + className.getClassName());
        }
    };

    /**
     * Handler of incoming messages from service.
     */
    class LocalHandler extends RemoteMessengerService.IncomingHandler {

        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            String playStr = b.getString(RemoteMessengerService.TV_PLAYER_EVENT_KEY);
            TvPlayerEvent playerEvent = gson.fromJson(playStr, TvPlayerEvent.class);
            if (playerEvent != null) {
                Timber.e(" 123r got in RemoteMessengerService   " + playerEvent.toString());
                sendToRemote(playerEvent);
            }
        }
    }


    public void sendToRemote(TvPlayerEvent event) {
        ServerEventStructure serverEventStructure = new ServerEventStructure(event.getPlayerAction(), event.getNewState());
        if (event.getPlayerPreview() != null) {
            LinkedList<PreviewCommonStructure> list = new LinkedList<>();
            list.add(event.getPlayerPreview());
            serverEventStructure.addPreviewCommonStructures(list);
        }
        server.postMessage(serverEventStructure);
    }


    private void registerMsgListener() {
        try {
            internalMsgr = new Messenger(new LocalHandler());
            Message msg = Message.obtain(null,
                    RemoteMessengerService.MSG_REGISTER_CLIENT);
            msg.replyTo = internalMsgr;

            remoteMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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

        if (remoteMessenger != null) {
            Message msg = Message.obtain(null,
                    RemoteMessengerService.MSG_UNREGISTER_CLIENT);
            try {
                remoteMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        dispose();
        Log.d("Log_ STOP ", "serverService stopped!!!");
        super.onDestroy();
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
        return START_STICKY;
    }

    @Override
    public void registerNsd(int port) {
        Timber.d("Register nsd");
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
        if (RemoteMessengerService.isStarted) {
            RemoteMessengerService.stop(context);
        }
        RemoteMessengerService.launch(context);
        //todo: need to restart service when device is connected not wifi change, for now not changing common flow that's why here.

        Intent launcher = new Intent(context, RemoteConlrolService.class);
        context.startService(launcher);
    }

    public static void stop(Context context) {
        Intent launcher = new Intent(context, RemoteConlrolService.class);
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


    private int parseIntOrLogError(String s) {
        try {
            if (s.contains(".")) Double.parseDouble(s);
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            Timber.e(e);
        }
        return 0;
    }


}