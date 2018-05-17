/*
 * Copyright (C) 2008-2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wezom.kiviremoteserver.keyboardsample.softkeyboard;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.text.InputType;
import android.text.method.MetaKeyKeyListener;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.wezom.kiviremoteserver.keyboardsample.R;
import com.wezom.kiviremoteserver.keyboardsample.softkeyboard.keymap.CyrillicKeyMap;

import java.util.HashMap;
import java.util.LinkedHashMap;

import timber.log.Timber;

/**
 * Example of writing an input method for a soft keyboard.  This code is
 * focused on simplicity over completeness, so it should in no way be considered
 * to be a complete soft keyboard implementation.  Its purpose is to provide
 * a basic example for how you would get started writing an input method, to
 * be fleshed out as appropriate.
 */
public class LatinIME extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {
    /**
     * This boolean indicates the optional example code for performing
     * processing of hard keys in addition to regular text generation
     * from on-screen interaction.  It would be used for input methods that
     * perform language translations (such as converting text entered on
     * a QWERTY keyboard to Chinese), but may not be used for input methods
     * that are primarily intended to be used for on-screen text entry.
     */
    static final boolean PROCESS_HARD_KEYS = true;

    public static final String KEYBOARD_EN = "english";
    public static final String KEYBOARD_RU = "russian";
    public static final String KEYBOARD_UA = "ukrainian";
    private static final String KEYBOARD_SYM = "symbols";
    private static final String KEYBOARD_SYM_SHIFTED = "symbols_shifted";

    private LatinKeyboardView mInputView;

    private StringBuilder mComposing = new StringBuilder();
    private boolean mPredictionOn;
    private boolean mCapsLock;
    private long mLastShiftTime;
    private long mMetaState;

    /* KEYBOARDS */
    private final HashMap<String, LatinKeyboard> keyboards = new LinkedHashMap<>();

    // Holds reference to latest selected keyboard
    private String currentTextKeyboard;
    private String currentKeyboardKey;
    private LatinKeyboard mCurKeyboard;

    private String mWordSeparators;

    private EditorInfo attributes;

    private AudioManager audioManager;


    /**
     * Main initialization of the input method component.  Be sure to call
     * to super class.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mWordSeparators = getResources().getString(R.string.word_separators);
    }

    private int getLayoutId() {
        return R.layout.input;
    }

    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    @Override
    public void onInitializeInterface() {
        keyboards.clear();
        keyboards.put(KEYBOARD_EN, new LatinKeyboard(this, R.xml.kbd_eng));
        keyboards.put(KEYBOARD_RU, new LatinKeyboard(this, R.xml.kbd_ru));
        keyboards.put(KEYBOARD_UA, new LatinKeyboard(this, R.xml.kbd_uk));
        keyboards.put(KEYBOARD_SYM, new LatinKeyboard(this, R.xml.symbols));
        keyboards.put(KEYBOARD_SYM_SHIFTED, new LatinKeyboard(this, R.xml.symbols_shift));
    }

    private void clearAllFocusedText(InputConnection ic) {
        ic.performContextMenuAction(android.R.id.selectAll);
        ic.commitText("", 0);
    }

    /**
     * Called by the framework when your view for creating input needs to
     * be generated.  This will be called the first time your input method
     * is displayed, and every time it needs to be re-created such as due to
     * a configuration change.
     */
    @Override
    public View onCreateInputView() {

        mInputView = null;
        mInputView = (LatinKeyboardView) getLayoutInflater().inflate(
                getLayoutId(), null, false);

        mInputView.setOnKeyboardActionListener(this);
        mInputView.setPreviewEnabled(false);

        if (currentKeyboardKey != null && !currentKeyboardKey.isEmpty()) {
            setLatinKeyboard(currentKeyboardKey);
        } else {
            setLatinKeyboard(KEYBOARD_EN);
        }
        return mInputView;
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    private void setLatinKeyboard(String key) {
        mCurKeyboard = keyboards.get(key);
        currentKeyboardKey = key;
        if (key.equals(KEYBOARD_EN) || key.equals(KEYBOARD_RU) || key.equals(KEYBOARD_UA))
            currentTextKeyboard = key;
        setLatinKeyboard(mCurKeyboard);
    }

    private void setLatinKeyboard(LatinKeyboard keyboard) {
        mInputView.setKeyboard(keyboard);
        if (attributes != null)
            keyboard.setImeOptions(getResources(), attributes.imeOptions);
    }

    private LatinKeyboard getCurrentKeyboard() {
        return mCurKeyboard;
    }

    /**
     * This is the main point where we do our initialization of the input method
     * to begin operating on an application.  At this point we have been
     * bound to the client, and are now receiving all of the detailed information
     * about the target of our edits.
     */
    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        attributes = attribute;

//        attributes.imeOptions &= ~IME_FLAG_NO_EXTRACT_UI;

        // Reset our state.  We want to do this even if restarting, because
        // the underlying state of the text editor could have changed in any way.
        mComposing.setLength(0);
        if (!restarting) {
            // Clear shift states.
            mMetaState = 0;
        }

        mPredictionOn = false;
        // We are now going to initialize our state based on the type of
        // text being edited.
        switch (attribute.inputType & InputType.TYPE_MASK_CLASS) {
            case InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_CLASS_DATETIME:
                // Numbers and dates default to the symbols keyboard, with
                // no extra features.
                mCurKeyboard = getKeyboard(KEYBOARD_SYM);
                currentKeyboardKey = KEYBOARD_SYM;
                break;

            case InputType.TYPE_CLASS_PHONE:
                // Phones will also default to the symbols keyboard, though
                // often you will want to have a dedicated phone keyboard.
                mCurKeyboard = getKeyboard(KEYBOARD_SYM);
                currentKeyboardKey = KEYBOARD_SYM;
                break;

            case InputType.TYPE_CLASS_TEXT:
                // This is general text editing.  We will default to the
                // normal alphabetic keyboard, and assume that we should
                // be doing predictive text (showing candidates as the
                // user types).
                if (mCurKeyboard == null) {
                    mCurKeyboard = getKeyboard(KEYBOARD_EN);
                    currentKeyboardKey = KEYBOARD_EN;
                }
                mPredictionOn = true;

                // We now look for a few special variations of text that will
                // modify our behavior.
                int variation = attribute.inputType & InputType.TYPE_MASK_VARIATION;
                if (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                        variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // Do not display predictions / what the user is typing
                    // when they are entering a password.
                    mPredictionOn = false;
                }

                if (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        || variation == InputType.TYPE_TEXT_VARIATION_URI
                        || variation == InputType.TYPE_TEXT_VARIATION_FILTER) {
                    // Our predictions are not useful for e-mail addresses
                    // or URIs.
                    mPredictionOn = false;
                    if (mCurKeyboard == null) {
                        mCurKeyboard = getKeyboard(KEYBOARD_EN);
                        currentKeyboardKey = KEYBOARD_EN;
                    }
                }

                if ((attribute.inputType & InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
                    // If this is an auto-complete text view, then our predictions
                    // will not be shown and instead we will allow the editor
                    // to supply their own.  We only show the editor's
                    // candidates when in fullscreen mode, otherwise relying
                    // own it displaying its own UI.
                    mPredictionOn = false;
                }

                // We also want to look at the current state of the editor
                // to decide whether our alphabetic keyboard should start out
                // shifted.
                updateShiftKeyState(attribute);
                break;

            default:
                // For all unknown input types, default to the alphabetic
                // keyboard with no special features.
                if (mCurKeyboard == null) {
                    mCurKeyboard = getKeyboard(KEYBOARD_EN);
                    currentKeyboardKey = KEYBOARD_EN;
                }
                updateShiftKeyState(attribute);
        }

        // Update the label on the enter key, depending on what the application
        // says it will do.
        mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);
    }

    /**
     * This is called when the user is done editing a field.  We can use
     * this to reset our state.
     */
    @Override
    public void onFinishInput() {
        super.onFinishInput();

        // Clear current composing text and candidates.
        mComposing.setLength(0);

        // We only hide the candidates window when finishing input on
        // a particular editor, to avoid popping the underlying application
        // up and down if the user is entering text into the bottom of
        // its window.
        setCandidatesViewShown(false);

        if (mCurKeyboard == null)
            mCurKeyboard = getKeyboard(KEYBOARD_EN);

        if (mInputView != null) {
            mInputView.closing();
        }
    }

    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        // Apply the selected keyboard to the input view.
        setLatinKeyboard(currentKeyboardKey);
//        EventBus.getDefault().post(new ShowKeyboardEvent());
        mInputView.closing();
    }

    /**
     * Deal with the editor reporting movement of its cursor.
     */
    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd,
                                  int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);

        // If the current selection in the text view changes, we should
        // clear whatever candidate text we have.
        if (mComposing.length() > 0 && (newSelStart != candidatesEnd
                || newSelEnd != candidatesEnd)) {
            mComposing.setLength(0);
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }
    }

    /**
     * This translates incoming hard key events in to edit operations on an
     * InputConnection.  It is only needed when using the
     * PROCESS_HARD_KEYS option.
     */
    private boolean translateKeyDown(int keyCode, KeyEvent event) {
        mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState,
                keyCode, event);
        int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState));
        mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
        InputConnection ic = getCurrentInputConnection();
        if (c == 0 || ic == null) {
            return false;
        }

        if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
        }

        if (mComposing.length() > 0) {
            char accent = mComposing.charAt(mComposing.length() - 1);
            int composed = KeyEvent.getDeadChar(accent, c);

            if (composed != 0) {
                c = composed;
                mComposing.setLength(mComposing.length() - 1);
            }
        }

        onKey(c, null);
        return true;
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CAPS_LOCK:
                handleLanguageSwitch();
                break;

            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                handleLanguageSwitch();
                break;

            case KeyEvent.KEYCODE_MEDIA_NEXT:
                handleLanguageSwitch();
                break;

            case KeyEvent.KEYCODE_DPAD_CENTER:
                keyDownUp(KeyEvent.KEYCODE_DPAD_CENTER);

//            case KeyEvent.KEYCODE_BACK:
//                // The InputMethodService already takes care of the back
//                // key for us, to dismiss the input method if it is shown.
//                // However, our keyboard could be showing a pop-up window
//                // that back should dismiss, so we first allow it to do that.
//                if (event.getRepeatCount() == 0 && mInputView != null && mInputView.handleBack()) {
//                    return true;
//                }
                break;

            case KeyEvent.KEYCODE_DEL:
                // Special handling of the delete key: if we currently are
                // composing text for the user, we want to modify that instead
                // of let the application to the delete itself.
                if (mComposing.length() > 0) {
                    onKey(Keyboard.KEYCODE_DELETE, null);
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_ENTER:
                // Let the underlying text editor always handle these.
                return false;

            case KeyEvent.KEYCODE_VOLUME_UP:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                break;

            case KeyEvent.KEYCODE_LANGUAGE_SWITCH:
                handleLanguageSwitch();
                return true;

//            case KeyEvent.KEYCODE_SHIFT_LEFT:
//            case KeyEvent.KEYCODE_SHIFT_RIGHT:
//                if (event.isAltPressed()) {
//                    handleLanguageSwitch();
//                    return true;
//                }
//                // NOTE: letting it fall-through to the other meta-keys
//            case KeyEvent.KEYCODE_ALT_LEFT:
//            case KeyEvent.KEYCODE_ALT_RIGHT:
//            case KeyEvent.KEYCODE_SYM:
//            case KeyEvent.KEYCODE_SPACE:
//                if ((event.isAltPressed())
//                        || event.isShiftPressed()) {
//                    handleLanguageSwitch();
//                    return true;
//                }
            default:
                // For all other keys, if we want to do transformations on
                // text being entered with a hard keyboard, we need to process
                // it and do the appropriate action.
//                if (PROCESS_HARD_KEYS) {
//                    if (keyCode == KeyEvent.KEYCODE_SPACE
//                            && (event.getMetaState() & KeyEvent.META_ALT_ON) != 0) {
//                        handleLanguageSwitch();
//                        translateKeyDown(keyCode, event);
//                    }
//                }
        }

        if (currentTextKeyboard != null)
            switch (currentTextKeyboard) {
                case KEYBOARD_RU:
                    Integer ruKey = CyrillicKeyMap.getRussianKeys().get(keyCode);
                    if (ruKey != null) {
                        sendKey(ruKey);
                        return true;
                    }
                    break;

                case KEYBOARD_UA:
                    Integer uaKey = CyrillicKeyMap.getUkrainanKeys().get(keyCode);
                    if (uaKey != null) {
                        sendKey(uaKey);
                        return true;
                    }
                    break;
            }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // If we want to do transformations on text being entered with a hard
        // keyboard, we need to process the up events to update the meta key
        // state we are tracking.
        if (PROCESS_HARD_KEYS) {
            if (mPredictionOn) {
                mMetaState = MetaKeyKeyListener.handleKeyUp(mMetaState,
                        keyCode, event);
            }
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                int volumeUp = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//                RxBus.INSTANCE.publish(new SendVolumeEvent(volumeUp));
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                int volumeDown = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//                RxBus.INSTANCE.publish(new SendVolumeEvent(volumeDown));
                break;
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * Helper function to commit any text being composed in to the editor.
     */
    private void commitTyped(InputConnection inputConnection) {
        if (mComposing.length() > 0) {
            inputConnection.commitText(mComposing, mComposing.length());
            mComposing.setLength(0);
        }
    }

    /**
     * Helper to update the shift state of our keyboard based on the initial
     * editor state.
     */
    private void updateShiftKeyState(EditorInfo attr) {
        if (attr != null &&
                mInputView != null &&
                (getKeyboard(KEYBOARD_EN) == mInputView.getKeyboard() ||
                        getKeyboard(KEYBOARD_RU) == mInputView.getKeyboard() ||
                        getKeyboard(KEYBOARD_UA) == mInputView.getKeyboard())) {
            int caps = 0;
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && ei.inputType != InputType.TYPE_NULL) {
                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
            }
            mInputView.setShifted(mCapsLock || caps != 0);
        }
    }

    /**
     * Helper to determine if a given character code is alphabetic.
     */
    private boolean isAlphabet(int code) {
        return Character.isLetter(code);
    }

    private boolean isDigit(int code) {
        return Character.isDigit(code);
    }

    /**
     * Helper to send a key down / key up pair to the current editor.
     */
    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

    private void keyDown(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
    }

    /**
     * Helper to send a character to the editor as raw key events.
     */
    private void sendKey(int keyCode) {
        switch (keyCode) {
            case '\n':
                keyDownUp(KeyEvent.KEYCODE_ENTER);
//                EventBus.getDefault().post(new HideKeyboardEvent());
                break;
            default:
                if (keyCode >= '0' && keyCode <= '9') {
                    keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
                    Timber.d("Key value: " + String.valueOf((char) keyCode));
                } else {
                    getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
                    Timber.d("Key value: " + String.valueOf((char) keyCode));
                }

                break;
        }
    }

    // Implementation of KeyboardViewListener

    public void onKey(int primaryCode, int[] keyCodes) {
        if (isWordSeparator(primaryCode)) {
            // Handle separator
            if (mComposing.length() > 0) {
                commitTyped(getCurrentInputConnection());
            }
            sendKey(primaryCode);
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
            handleBackspace();
        } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
            handleShift();
        } else if (primaryCode == Keyboard.KEYCODE_CANCEL) {
            handleClose();
        } else if (primaryCode == LatinKeyboardView.KEYCODE_LANGUAGE_SWITCH) {
            handleLanguageSwitch();
        } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE
                && mInputView != null) {
            Keyboard current = mInputView.getKeyboard();
            if (current == getKeyboard(KEYBOARD_SYM) || current == getKeyboard(KEYBOARD_SYM_SHIFTED)) {
                setLatinKeyboard(currentTextKeyboard);
            } else {
                setLatinKeyboard(KEYBOARD_SYM);
            }
        } else {
            handleCharacter(primaryCode, keyCodes);
        }
    }

    public void onText(CharSequence text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.beginBatchEdit();
        if (mComposing.length() > 0) {
            commitTyped(ic);
        }
        ic.commitText(text, 0);
        ic.endBatchEdit();
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    private void handleBackspace() {
        final int length = mComposing.length();
        if (length > 1) {
            mComposing.delete(length - 1, length);
            getCurrentInputConnection().setComposingText(mComposing, 1);
        } else if (length > 0) {
            mComposing.setLength(0);
            getCurrentInputConnection().commitText("", 0);
        } else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    private void handleShift() {
        if (mInputView == null) {
            return;
        }

        Keyboard currentKeyboard = mInputView.getKeyboard();
        if (getKeyboard(KEYBOARD_EN) == currentKeyboard ||
                getKeyboard(KEYBOARD_RU) == currentKeyboard ||
                getKeyboard(KEYBOARD_UA) == currentKeyboard) {
            // Alphabet keyboard
            checkToggleCapsLock();
            mInputView.setShifted(mCapsLock || !mInputView.isShifted());
        } else if (currentKeyboard == getKeyboard(KEYBOARD_SYM)) {
            setLatinKeyboard(KEYBOARD_SYM_SHIFTED);
        } else if (currentKeyboard == getKeyboard(KEYBOARD_SYM_SHIFTED)) {
            setLatinKeyboard(KEYBOARD_SYM);
        }
    }

    private void handleShiftWithoutDelay() {
        if (mInputView == null) {
            return;
        }

        Keyboard currentKeyboard = mInputView.getKeyboard();
        if (getKeyboard(KEYBOARD_EN) == currentKeyboard ||
                getKeyboard(KEYBOARD_RU) == currentKeyboard ||
                getKeyboard(KEYBOARD_UA) == currentKeyboard) {
            // Alphabet keyboard
            toggleCapsLock();
            mInputView.setShifted(mCapsLock || !mInputView.isShifted());
        } else if (currentKeyboard == getKeyboard(KEYBOARD_SYM)) {
            setLatinKeyboard(KEYBOARD_SYM_SHIFTED);
        } else if (currentKeyboard == getKeyboard(KEYBOARD_SYM_SHIFTED)) {
            setLatinKeyboard(KEYBOARD_SYM);
        }
    }

    private void handleCharacter(int primaryCode, int[] keyCodes) {
        if (isInputViewShown()) {
            if (mInputView.isShifted()) {
                primaryCode = Character.toUpperCase(primaryCode);
            }
        }
        if ((isAlphabet(primaryCode) || isDigit(primaryCode)) && mPredictionOn) {
            mComposing.append((char) primaryCode);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else {
            getCurrentInputConnection().commitText(
                    String.valueOf((char) primaryCode), 1);
        }
    }

    private void handleClose() {
        commitTyped(getCurrentInputConnection());
        requestHideSelf(0);
        mInputView.closing();
    }

    // Switch input language
    private void handleLanguageSwitch() {
        getCurrentKeyboard().setShifted(false);
        mCapsLock = false;

        if (getCurrentKeyboard() == getKeyboard(KEYBOARD_EN)) {
            setLatinKeyboard(KEYBOARD_RU);
            return;
        }

        if (getCurrentKeyboard() == getKeyboard(KEYBOARD_RU)) {
            setLatinKeyboard(KEYBOARD_UA);
            return;
        }

        if (getCurrentKeyboard() == getKeyboard(KEYBOARD_UA)) {
            setLatinKeyboard(KEYBOARD_EN);
            return;
        }
    }

    private void checkToggleCapsLock() {
        long now = System.currentTimeMillis();
        if (mCapsLock) {
            mCapsLock = false;
        } else if (mLastShiftTime + 400 > now) {
            mCapsLock = true;
            mLastShiftTime = 0;
        } else {
            mLastShiftTime = now;
        }
    }

    private void toggleCapsLock() {
        mCapsLock = !mCapsLock;
    }

    private LatinKeyboard getKeyboard(String key) {
        return keyboards.get(key);
    }

    private LatinKeyboardView getInputView() {
        return mInputView;
    }

    private String getWordSeparators() {
        return mWordSeparators;
    }

    public boolean isWordSeparator(int code) {
        String separators = getWordSeparators();
        return separators.contains(String.valueOf((char) code));
    }

    public void swipeRight() {
        handleLanguageSwitch();
    }

    public void swipeLeft() {
        handleLanguageSwitch();
    }

    public void swipeDown() {
        handleClose();
    }

    public void swipeUp() {
    }

    public void onPress(int primaryCode) {
    }

    public void onRelease(int primaryCode) {
    }
}
