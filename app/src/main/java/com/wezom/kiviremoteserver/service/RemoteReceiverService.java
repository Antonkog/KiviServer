package com.wezom.kiviremoteserver.service;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.wezom.kiviremoteserver.App;
import com.wezom.kiviremoteserver.bus.ExecutorPlayerEvent;
import com.wezom.kiviremoteserver.bus.KeyboardEvent;
import com.wezom.kiviremoteserver.bus.NewDataEvent;
import com.wezom.kiviremoteserver.bus.PingEvent;
import com.wezom.kiviremoteserver.bus.SendAppsListEvent;
import com.wezom.kiviremoteserver.bus.SendAspectEvent;
import com.wezom.kiviremoteserver.bus.SendImgByIds;
import com.wezom.kiviremoteserver.bus.SendInitialEvent;
import com.wezom.kiviremoteserver.bus.SendVolumeEvent;
import com.wezom.kiviremoteserver.bus.ShowHideAspectEvent;
import com.wezom.kiviremoteserver.bus.ToKeyboardExecutorEvent;
import com.wezom.kiviremoteserver.common.AppsInfoLoader;
import com.wezom.kiviremoteserver.common.Constants;
import com.wezom.kiviremoteserver.common.DeviceUtils;
import com.wezom.kiviremoteserver.common.MotionRelay;
import com.wezom.kiviremoteserver.common.RxBus;
import com.wezom.kiviremoteserver.common.Utils;
import com.wezom.kiviremoteserver.interfaces.DataStructure;
import com.wezom.kiviremoteserver.interfaces.InitialMessage;
import com.wezom.kiviremoteserver.net.server.model.LauncherBasedData;
import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper;

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
import static com.wezom.kiviremoteserver.net.nsd.NsdUtil.DEVICE_NAME_KEY;

/***
 * see link: https://developer.android.com/guide/components/bound-services#Messenger
 */
public class RemoteReceiverService extends Service {
    CompositeDisposable disposables;
    public static boolean isStarted = false;

    @Inject
    Instrumentation instrumentation;

    @Inject
    InputSourceHelper inputSourceHelper;

    @Inject
    AppsInfoLoader appsInfoLoader;

    @Inject
    DeviceUtils deviceUtils;

    @Inject
    AudioManager audioManager;

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

    @Override
    public void onCreate() {
        super.onCreate();
        App.getApplicationComponent().inject(this);
        dispose();
        disposables = new CompositeDisposable();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isStarted = true;
        Timber.d("create IME_Service");
        initObservers();
    }

    @Override
    public void onDestroy() {
        isStarted = false;
        Log.d("Log_ STOP ", "RemoteReceiverService stopped!!!");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void initObservers() {
        disposables.add(RxBus.INSTANCE
                .listen(NewDataEvent.class)
                .map(NewDataEvent::getDataEvent)
                .subscribe(this::handleRequest, Timber::e));

        disposables.add(RxBus.INSTANCE
                .listen(KeyboardEvent.class).subscribe(keyboardEvent -> {
                    if (keyboardEvent.isMuteEvent()) muteWorkAround();
                    else sendVolume();
                }, Timber::e));

    }

    public static void launch(Context context) {
        Intent launcher = new Intent(context, RemoteReceiverService.class);
        context.startService(launcher);
    }

    public static void stop(Context context) {
        Intent launcher = new Intent(context, RemoteReceiverService.class);
        context.stopService(launcher);
    }

    public void handleRequest(DataStructure dataStructure) {
        Timber.d("Received action: " + dataStructure.getAction() + " request");

        if (dataStructure.getAction() != null) {
            switch (dataStructure.getAction()) {
                case KEY_EVENT:
                case keyevent:
                    executeCommand(ToKeyboardExecutorEvent.CommandType.COMMAND_NORMAL, parseIntOrLogError(dataStructure.getArgs().get(0)), null);
                    break;

                case TEXT:
                case text:
                    executeCommand(ToKeyboardExecutorEvent.CommandType.TEXT, 0, dataStructure.getArgs().get(0));
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
                    executeCommand(ToKeyboardExecutorEvent.CommandType.CLICK, 0, null);

                    break;

                case RIGHT_CLICK:
                case rightClick:
                    executeCommand(ToKeyboardExecutorEvent.CommandType.COMMAND_NORMAL, KeyEvent.KEYCODE_BACK, null);
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
                                executeCommand(ToKeyboardExecutorEvent.CommandType.COMMAND_NORMAL, KeyEvent.KEYCODE_DPAD_DOWN, null);
                            } else {
                                executeCommand(ToKeyboardExecutorEvent.CommandType.COMMAND_NORMAL, KeyEvent.KEYCODE_DPAD_UP, null);
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
                    disposableInit =
                            InitialMessage.getInstance().setDriverValueListSingle(getApplicationContext()).
                                    subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            initialMessage -> RxBus.INSTANCE.publish(new SendInitialEvent(initialMessage)),
                                            e -> Timber.e(e, e.getMessage()));
                    break;
                case REQUEST_INITIAL_II:
                    dispose(disposableInit_II);
                    disposableInit_II =
                            deviceUtils.getPreviewCommonStructureSingle(getApplicationContext())
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
                    RxBus.INSTANCE.publish(new ExecutorPlayerEvent(dataStructure.getArgs().get(0), dataStructure.getMotion()));
                    break;
                case CHANGE_INPUT:
                    changeInput(parseIntOrLogError(dataStructure.getArgs().get(0)));
                    break;
                default:
                    Timber.e("some not handled action in IME => %s", dataStructure.getAction());
                    break;
            }
        } else {
            Timber.e("server got message but dataStructure.getAction() == null");
        }
    }

    private void executeCommand(ToKeyboardExecutorEvent.CommandType type, int keyCode, String text) {
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

    private void volumeWorkAround(boolean volumeUp) {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int newVolume = currentVolume + (volumeUp ? +1 : -1);
        if (newVolume > 100) newVolume = 100;
        if (newVolume < 0) newVolume = 0;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, AudioManager.FLAG_SHOW_UI);
        prefs.edit().putInt(LAST_VOLUME, currentVolume).apply();
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
//To decrease media player volume
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
        sendVolume();
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


    private int parseIntOrLogError(String s) {
        try {
            if (s.contains(".")) Double.parseDouble(s);
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            Timber.e(e);
        }
        return 0;
    }

    private void changeInput(int inputId) {
        inputSourceHelper.changeInput(inputId, getApplicationContext());
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
}
