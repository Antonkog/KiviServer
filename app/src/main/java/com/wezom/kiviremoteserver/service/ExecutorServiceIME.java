package com.wezom.kiviremoteserver.service;

import android.app.Instrumentation;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.android.inputmethod.pinyin.PinyinIME;
import com.wezom.kiviremoteserver.App;
import com.wezom.kiviremoteserver.bus.HideKeyboardEvent;
import com.wezom.kiviremoteserver.bus.KeyboardEvent;
import com.wezom.kiviremoteserver.bus.ShowKeyboardEvent;
import com.wezom.kiviremoteserver.bus.ToKeyboardExecutorEvent;
import com.wezom.kiviremoteserver.common.MotionRelay;
import com.wezom.kiviremoteserver.common.RxBus;
import com.wezom.kiviremoteserver.interfaces.EventProtocolExecutor;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by andre on 06.06.2017.
 */

public class ExecutorServiceIME extends PinyinIME implements EventProtocolExecutor {

    @Inject
    Instrumentation instrumentation;

    private CompositeDisposable disposables;

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
        initObservers();
        App.getApplicationComponent().inject(this);
        Timber.d("create IME_Service");
    }


    private void dispose() {
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
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

    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }


    private void initObservers() {
        disposables.add(RxBus.INSTANCE
                .listen(ToKeyboardExecutorEvent.class)
                .subscribe(this::handleRequest, Timber::e));
    }

    private void handleRequest(ToKeyboardExecutorEvent toKeyboardExecutorEvent) {
        executeCommand(toKeyboardExecutorEvent.getKeyCode());
    }


    @Override
    public void executeCommand(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            getCurrentInputConnection().performEditorAction(EditorInfo.IME_ACTION_UNSPECIFIED);
            return;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            keyDownUp(KeyEvent.KEYCODE_VOLUME_DOWN);
            sendVolume(false);
            return;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            keyDownUp(KeyEvent.KEYCODE_VOLUME_UP);
            sendVolume(false);
            return;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            sendVolume(true); //        keyDownUp(KeyEvent.KEYCODE_VOLUME_MUTE); not working on realtek
            return;
        }
        onVolumeChange(keyCode);
    }

    private void onVolumeChange(int keyCode) {
        Completable
                .fromCallable(() -> executeKeyDownUp(keyCode))
                .subscribeOn(Schedulers.newThread())
                .doOnError(e -> Timber.e(e, e.getMessage()))
                .subscribe();
    }

    private int executeKeyDownUp(int keyCode) {
        try {
            instrumentation.sendKeyDownUpSync(keyCode);
        } catch (SecurityException e) {
            Timber.e(e, "Failed to inject event: " + e.getMessage());
        }
        return keyCode;
    }

    private void sendVolume(boolean isMuteEvent) {
        RxBus.INSTANCE.publish(new KeyboardEvent(isMuteEvent));
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

    @Override
    public void onBindInput() {
        super.onBindInput();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                sendVolume(false);
                break;
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                sendVolume(true);
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

}
