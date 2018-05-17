package com.wezom.kiviremoteserver.service;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.android.inputmethod.pinyin.PinyinIME;
import com.wezom.kiviremoteserver.bus.HideKeyboardEvent;
import com.wezom.kiviremoteserver.bus.NewDataEvent;
import com.wezom.kiviremoteserver.bus.PingEvent;
import com.wezom.kiviremoteserver.bus.SendAppsListEvent;
import com.wezom.kiviremoteserver.bus.SendVolumeEvent;
import com.wezom.kiviremoteserver.bus.ShowKeyboardEvent;
import com.wezom.kiviremoteserver.common.DeviceUtils;
import com.wezom.kiviremoteserver.common.MotionRelay;
import com.wezom.kiviremoteserver.common.RxBus;
import com.wezom.kiviremoteserver.common.Utils;
import com.wezom.kiviremoteserver.interfaces.DataStructure;
import com.wezom.kiviremoteserver.interfaces.EventProtocolExecutor;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by andre on 06.06.2017.
 */

public class ExecutorServiceIME extends PinyinIME implements EventProtocolExecutor {

    private AudioManager audioManager;

    private Disposable scrollDisposable;
    private Disposable requestAppsDisposable;

    private final int width = Resources.getSystem().getDisplayMetrics().widthPixels;
    private final int height = Resources.getSystem().getDisplayMetrics().heightPixels;
    private final int x = width / 2;
    private final int y = height / 2;

    private CompositeDisposable disposables;
    private final Instrumentation instrumentation = new Instrumentation();

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

    /**
     * Helper to send a character to the editor as raw key events.
     */
    private void sendKey(int keyCode) {
        if (keyCode >= '0' && keyCode <= '9') {
            keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
            Timber.d("Key value: " + String.valueOf((char) keyCode));
        } else {
            getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
            Timber.d("Key value: " + String.valueOf((char) keyCode));
        }
    }

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
            keyDownUp(KeyEvent.KEYCODE_VOLUME_MUTE);
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
                        RxBus.INSTANCE.publish(new SendVolumeEvent(1));
                    }

                }, Timber::e
        );
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

        if (dataStructure.getAction() != null)
            switch (dataStructure.getAction()) {
                case KEY_EVENT:
                case keyevent:
                    executeCommand(Integer.parseInt(dataStructure.getArgs().get(0)));
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

                case REQUEST_APPS:
                    if (requestAppsDisposable != null && !requestAppsDisposable.isDisposed()) {
                        requestAppsDisposable.dispose();
                    }

                    requestAppsDisposable = Observable
                            .fromCallable(() -> DeviceUtils.getInstalledApplications(this, getPackageManager()))
                            .subscribeOn(Schedulers.io())
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
                    launchApp("com.funshion.poweroffdialog");
                    break;

                case SCROLL:
                    scroll(dataStructure.getMotion().get(1));
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

                default:
                    Timber.e("some not handled action in IME => %s", dataStructure.getAction());
                    break;
            }
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
