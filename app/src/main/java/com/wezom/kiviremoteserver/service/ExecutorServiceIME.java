package com.wezom.kiviremoteserver.service;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.android.inputmethod.pinyin.PinyinIME;
import com.wezom.kiviremoteserver.bus.HideKeyboardEvent;
import com.wezom.kiviremoteserver.bus.NewDataEvent;
import com.wezom.kiviremoteserver.bus.PingEvent;
import com.wezom.kiviremoteserver.bus.SendAppsListEvent;
import com.wezom.kiviremoteserver.bus.SendAspectEvent;
import com.wezom.kiviremoteserver.bus.SendChannelsEvent;
import com.wezom.kiviremoteserver.bus.SendFavouritesEvent;
import com.wezom.kiviremoteserver.bus.SendInitialEvent;
import com.wezom.kiviremoteserver.bus.SendInputsEvent;
import com.wezom.kiviremoteserver.bus.SendRecommendationsEvent;
import com.wezom.kiviremoteserver.bus.SendVolumeEvent;
import com.wezom.kiviremoteserver.bus.ShowHideAspectEvent;
import com.wezom.kiviremoteserver.bus.ShowKeyboardEvent;
import com.wezom.kiviremoteserver.common.AppsInfoLoader;
import com.wezom.kiviremoteserver.common.Constants;
import com.wezom.kiviremoteserver.common.DeviceUtils;
import com.wezom.kiviremoteserver.common.MotionRelay;
import com.wezom.kiviremoteserver.common.RxBus;
import com.wezom.kiviremoteserver.common.Utils;
import com.wezom.kiviremoteserver.common.extensions.ViewExtensionsKt;
import com.wezom.kiviremoteserver.interfaces.DataStructure;
import com.wezom.kiviremoteserver.interfaces.EventProtocolExecutor;
import com.wezom.kiviremoteserver.interfaces.InitialMessage;
import com.wezom.kiviremoteserver.net.server.model.LauncherBasedData;
import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.wezom.kiviremoteserver.common.Constants.LAST_VOLUME;
import static com.wezom.kiviremoteserver.net.nsd.NsdUtil.DEVICE_NAME_KEY;

/**
 * Created by andre on 06.06.2017.
 */

public class ExecutorServiceIME extends PinyinIME implements EventProtocolExecutor {

    private AudioManager audioManager;
    private InputSourceHelper inputSourceHelper = null;

    private Disposable scrollDisposable;
    private Disposable requestAppsDisposable;

    private final int width = Resources.getSystem().getDisplayMetrics().widthPixels;
    private final int height = Resources.getSystem().getDisplayMetrics().heightPixels;
    private final int x = width / 2;
    private final int y = height / 2;
    private final int DEFAULT_PREF_VOLUME = -2;
    private CompositeDisposable disposables;
    private final Instrumentation instrumentation = new Instrumentation();
    private SharedPreferences prefs;
    private long scrollTime = System.currentTimeMillis();
    private Disposable disposableInit_II, disposableInit = null;


    @Override
    public void onWindowShown() {
        super.onWindowShown();
        Timber.d("onWindowShown has been called");
        RxBus.INSTANCE.publish(new ShowKeyboardEvent());
    }

    @Override
    public void onWindowHidden() {
        super.onWindowHidden();
        Timber.d("onWindowHidden has been called");
        RxBus.INSTANCE.publish(new HideKeyboardEvent());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dispose();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        disposables = new CompositeDisposable();
        Timber.d("create IME_Service");
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        initObservers();
    }

    private void initObservers() {
        disposables.add(RxBus.INSTANCE
                .listen(NewDataEvent.class)
                .map(NewDataEvent::getDataEvent)
                .subscribe(this::handleRequest, Timber::e));
    }

    @Override
    public void onDestroy() {
        dispose();
        super.onDestroy();
    }

    @Override
    public void executeTextCommand(String text) {
        InputConnection conn = getCurrentInputConnection();

        if (conn == null) {
            Timber.e("Input connection is null!");
            return;
        }

        if (text.isEmpty()) {
            clearAllFocusedText(conn);
        } else {
            conn.setComposingText(text, text.length());
        }
    }

    private void clearAllFocusedText(InputConnection ic) {
        ic.performContextMenuAction(android.R.id.selectAll);
        ic.commitText("", 0);
    }

    //
//    /**
//     * Helper to send a character to the editor as raw key events.
//     */
//    private void sendKey(int keyCode) {
//        if (keyCode >= '0' && keyCode <= '9') {
//            keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
//            Timber.d("Key value: " + String.valueOf((char) keyCode));
//        } else {
//            getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
//            Timber.d("Key value: " + String.valueOf((char) keyCode));
//        }
//    }
//
    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
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

    @Override
    public void executeCommand(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            getCurrentInputConnection().performEditorAction(EditorInfo.IME_ACTION_UNSPECIFIED);
            return;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            keyDownUp(KeyEvent.KEYCODE_VOLUME_DOWN);
            sendVolume();
            return;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            keyDownUp(KeyEvent.KEYCODE_VOLUME_UP);
            sendVolume();
            return;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            muteWorkAround(); //        keyDownUp(KeyEvent.KEYCODE_VOLUME_MUTE); not working on realtek
            sendVolume();
            return;
        }

        Completable
                .fromCallable(() -> executeKeyDownUp(keyCode))
                .subscribeOn(Schedulers.newThread())
                .doOnError(e -> Timber.e(e, e.getMessage()))
                .subscribe();
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

    private int executeKeyDownUp(int keyCode) {
        try {
            instrumentation.sendKeyDownUpSync(keyCode);
        } catch (SecurityException e) {
            Timber.e(e, "Failed to inject event: " + e.getMessage());
        }
        return keyCode;
    }

    @Override
    public void executeClickCommand() {
        try {
            MotionRelay.INSTANCE
                    .getRelay()
                    .accept(new MotionRelay.CursorMotionEvent(MotionRelay.LEFT_CLICK));
        } catch (Exception e) {
            Timber.e(e, e.getMessage());
        }
    }

    private void scroll(float y) {
        dispose(scrollDisposable);
        scrollDisposable = Completable
                .fromCallable(() -> executeScroll(y))
                .subscribeOn(Schedulers.io())
                .doOnError(e -> Timber.e(e, e.getMessage()))
                .subscribe();
    }

    private void dispose(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
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

    @Override
    public void handleRequest(DataStructure dataStructure) {
        Timber.d("Received action: " + dataStructure.getAction() + " request");

        if (dataStructure.getAction() != null) {
            switch (dataStructure.getAction()) {
                case KEY_EVENT:
                case keyevent:
                    executeCommand(parseIntOrLogError(dataStructure.getArgs().get(0)));
                    break;

                case TEXT:
                case text:
                    executeTextCommand(dataStructure.getArgs().get(0));
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
                    executeClickCommand();
                    break;

                case RIGHT_CLICK:
                case rightClick:
                    executeCommand(KeyEvent.KEYCODE_BACK);
                    break;

                case VOICE_SEARCH:
                    ViewExtensionsKt.toastOutsource(getBaseContext(), dataStructure.getAction().name() + " " + dataStructure.getArgs().get(0));
                    break;

                case SET_VOLUME:
                    int volume = parseIntOrLogError(dataStructure.getArgs().get(0));
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
                    Timber.d("SET_VOLUME" + volume);
                    break;

                case REQUEST_APPS:
                    if (requestAppsDisposable != null && !requestAppsDisposable.isDisposed()) {
                        requestAppsDisposable.dispose();
                    }

                    requestAppsDisposable = AppsInfoLoader.getAppsList(getApplicationContext())
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
                            if (dy > 0) executeCommand(KeyEvent.KEYCODE_DPAD_DOWN);
                            else executeCommand(KeyEvent.KEYCODE_DPAD_UP);
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
                    disposables.add(disposableInit);
                    ViewExtensionsKt.toastOutsource(getBaseContext(), dataStructure.getAction().name());
                    break;
                case REQUEST_INITIAL_II:
                    dispose(disposableInit_II);
                    disposableInit_II =
                            DeviceUtils.getPreviewCommonStructureSingle(getApplicationContext()).
                                    subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            previewCommonStructures -> {
                                                RxBus.INSTANCE.publish(new SendInitialEvent(previewCommonStructures));
                                            },
                                            e -> Timber.e(e, e.getMessage()));
                    disposables.add(disposableInit_II);
                    ViewExtensionsKt.toastOutsource(getBaseContext(), dataStructure.getAction().name());
                    break;
                case REQUEST_CHANNELS:
                    RxBus.INSTANCE.publish(new SendChannelsEvent());
                    break;
                case LAUNCH_CHANNEL:
                    startLauncherIntent(LauncherBasedData.TYPE.CHANNEL, dataStructure.getArgs().get(0));
                    ViewExtensionsKt.toastOutsource(getBaseContext(), dataStructure.getAction().name() + " " + dataStructure.getArgs().get(0));
                    break;
                case REQUEST_RECOMMENDATIONS:
                    RxBus.INSTANCE.publish(new SendRecommendationsEvent());
                    break;
                case REQUEST_FAVORITES:
                    RxBus.INSTANCE.publish(new SendFavouritesEvent());
                    break;
                case LAUNCH_RECOMMENDATION:
                    startLauncherIntent(LauncherBasedData.TYPE.RECOMMENDATION, dataStructure.getArgs().get(0));
                    ViewExtensionsKt.toastOutsource(getBaseContext(), dataStructure.getAction().name() + " " + dataStructure.getArgs().get(0));
                    break;
                case LAUNCH_FAVORITE:
                    startLauncherIntent(LauncherBasedData.TYPE.FAVOURITE, dataStructure.getArgs().get(0));
                    break;
                case REQUEST_INPUTS:
                    Timber.d("Inputs requested");
                    RxBus.INSTANCE.publish(new SendInputsEvent());
                    break;
                case CHANGE_INPUT:
                    changeInput(parseIntOrLogError(dataStructure.getArgs().get(0)));
                    break;
                default:
                    Timber.e("some not handled action in IME => %s", dataStructure.getAction());
                    break;
            }
        } else {
            ViewExtensionsKt.toastOutsource(getBaseContext(), "server got message but dataStructure.getAction() == null");
        }
    }

    private int parseIntOrLogError(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            Timber.e(e);
        }
        return 0;
    }

    private void changeInput(int inputId) {
        if (inputSourceHelper == null)
            inputSourceHelper = new InputSourceHelper();
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

    @Override
    public void onBindInput() {
        super.onBindInput();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                sendVolume();
                break;
        }
        return super.onKeyUp(keyCode, event);
    }
}
