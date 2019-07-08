/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.inputmethod.pinyin;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.android.inputmethod.pinyin.inputprocessors.InputProcessor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Main class of the Pinyin input method.
 */
public class PinyinIME extends InputMethodService {
    /**
     * TAG for debug.
     */
    static final String TAG = "PinyinIME";

    /**
     * Define the key of keyboard: UP, DOWN, LEFT, RIGHT
     */
    private static final int KEY_UP = 0x121;
    private static final int KEY_DOWN = 0x122;
    private static final int KEY_LEFT = 0x123;
    private static final int KEY_RIGHT = 0x124;

    private static boolean isAfter = false;
    private static boolean isShowInputView = false;
    private static boolean isSimulateEnterUp = false;

    private static boolean skbFocusEnable = true;
    private static boolean candFocusEnable = false;
    private static boolean isInputting = false;

    private static final boolean SIMULATE_KEY_DELETE = false;

    /**
     * Necessary environment configurations like screen size for this IME.
     */
    private Environment mEnvironment;

    /**
     * Used to switch input mode.
     */
    private InputModeSwitcher mInputModeSwitcher;

    /**
     * Soft keyboard container view to host real soft keyboard view.
     */
    private SkbContainer mSkbContainer;

    /**
     * The floating container which contains the composing view. If necessary,
     * some other view like candiates container can also be put here.
     */
    private LinearLayout mFloatingContainer;

    /**
     * View to show the composing string.
     */
    private ComposingView mComposingView;

    /**
     * Window to show the composing string.
     */
    private PopupWindow mFloatingWindow;

    /**
     * Used to show the floating window.
     */
    private PopupTimer mFloatingWindowTimer = new PopupTimer();

    /**
     * View to show candidates list.
     */
    private CandidatesContainer mCandidatesContainer;

    /**
     * Balloon used when user presses a candidate.
     */
    private BalloonHint mCandidatesBalloon;

    /**
     * Used to notify the input method when the user touch a candidate.
     */
    private ChoiceNotifier mChoiceNotifier;

    /**
     * Used to notify gestures from soft keyboard.
     */
    private OnGestureListener mGestureListenerSkb;

    /**
     * Used to notify gestures from candidates view.
     */
    private OnGestureListener mGestureListenerCandidates;

    /**
     * The on-screen movement gesture detector for soft keyboard.
     */
    private GestureDetector mGestureDetectorSkb;

    /**
     * The on-screen movement gesture detector for candidates view.
     */
    private GestureDetector mGestureDetectorCandidates;

    /**
     * Option dialog to choose settings and other IMEs.
     */
    private AlertDialog mOptionsDialog;

    /**
     * The current IME status.
     *
     * @see com.android.inputmethod.pinyin.PinyinIME.ImeState
     */
    private ImeState mImeState = ImeState.STATE_IDLE;

    /**
     * The decoding information, include spelling(Pinyin) string, decoding
     * result, etc.
     */
    private DecodingInfo mDecInfo = new DecodingInfo();

    private InputProcessor inputProcessor;

    private static long oldTime = 0;
    private static long curTime = 0;
    private static int oldKeycode = -100;
    private boolean isRepeat = false;
    private boolean isSimulate = false;
    private static int counter = 0;

    private final Pair<Integer, Integer> defaultPair = new Pair<>(3, 5);

    // receive ringer mode changes
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SoundManager.getInstance(context).updateRingerMode();
        }
    };

    @Override
    public void onCreate() {
        mEnvironment = Environment.getInstance();
        if (mEnvironment.needDebug()) {
            //Log.d(TAG, "onCreate.");
        }
        super.onCreate();

        inputProcessor = new InputProcessor();

        Settings.getInstance(PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()));

        mInputModeSwitcher = new InputModeSwitcher(this);
        mChoiceNotifier = new ChoiceNotifier(this);
        mGestureListenerSkb = new OnGestureListener(false);
        mGestureListenerCandidates = new OnGestureListener(true);
        mGestureDetectorSkb = new GestureDetector(this, mGestureListenerSkb);
        mGestureDetectorCandidates = new GestureDetector(this,
                mGestureListenerCandidates);

        mEnvironment.onConfigurationChanged(getResources().getConfiguration(),
                this);
    }

    @Override
    public void onDestroy() {
        if (mEnvironment.needDebug()) {
            Log.d(TAG, "onDestroy.");
        }
        Settings.releaseInstance();
        super.onDestroy();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Environment env = Environment.getInstance();
        if (mEnvironment.needDebug()) {
            Log.d(TAG, "onConfigurationChanged");
            Log.d(TAG, "--last config: " + env.getConfiguration().toString());
            Log.d(TAG, "---new config: " + newConfig.toString());
        }
        // We need to change the local environment first so that UI components
        // can get the environment instance to handle size issues. When
        // super.onConfigurationChanged() is called, onCreateCandidatesView()
        // and onCreateInputView() will be executed if necessary.
        env.onConfigurationChanged(newConfig, this);

        // Clear related UI of the previous configuration.
        if (null != mSkbContainer) {
            mSkbContainer.dismissPopups();
        }
        if (null != mCandidatesBalloon) {
            mCandidatesBalloon.dismiss();
        }
        super.onConfigurationChanged(newConfig);
        resetToIdleState(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CAPS_LOCK:
                switchLanguage();
                return true;
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                if (event.isCtrlPressed()) {
                    switchLanguage();
                    return true;
                } else break;
        }

        if (mSkbContainer != null) {
            curTime = System.currentTimeMillis();
            if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)
                    && !isSimulate) {
                if ((curTime - oldTime < 400) && oldKeycode == keyCode) {
                    isRepeat = true;
                    return true;
                } else {
                    isRepeat = false;
                }
            }

            isSimulateEnterUp = false;
            if ((curTime - oldTime < 250) && !isSimulate && counter >= 1) {
                isRepeat = true;
                isSimulate = false;
                counter = 0;
                return true;
            } else {
                isSimulate = false;
                isRepeat = false;
                if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) && event.getRepeatCount() > 0) {
                    isSimulateEnterUp = true;
                }
                if (oldKeycode == keyCode) {
                    counter++;
                } else {
                    counter = 0;
                }
            }

            isAfter = false;
            skbFocusEnable = mSkbContainer.getInputViewFocusable();
            candFocusEnable = (null != mCandidatesContainer) && (mCandidatesContainer.isShown());

            if (!candFocusEnable) {
                skbFocusEnable = true;
                mSkbContainer.setInputViewFocusable(true);
            }

            oldTime = curTime;

            if (processKey(event, 0 != event.getRepeatCount())) {
                return true;
            }
        }
//        else {
//            return super.onKeyDown(mapkeyForHKB(event), event);
//        }
        return super.onKeyDown(keyCode, event);
    }

    private void switchLanguage() {
        mInputModeSwitcher.switchLanguage();
        if (mSkbContainer != null)
            mSkbContainer.updateInputMode();
        if (mDecInfo.isCandidatesListEmpty()) {
            dismissCandidateWindow();
        }
    }

    /**
     * when keyboard is shown
     * imput processor works
     * and give same values, as soft keyboard shown,
     * will make output same as for soft keyboard.
     * But not sure, that is right way see google docs:
     * https://source.android.com/devices/input/key-character-map-files.html
     * hardware keyboard should have hard Key Mapping for extra languages.
     */
//    @Override
//    public boolean onEvaluateInputViewShown() {
//        return languageSwitched; //true
//    }

/*this is wrong and should not be used:
as hardKeyboarrd is hard, not soft.
see:  https://source.android.com/devices/input/key-character-map-files.html

private int mapkeyForHKB(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int keyCodeMapped = keyCode;

        switch (mInputModeSwitcher.getInputMode()) {
//            case InputModeSwitcher.MODE_SKB_ENGLISH_LOWER:
//            case  InputModeSwitcher.MODE_SKB_ENGLISH_UPPER:
//                break;
            case InputModeSwitcher.MODE_SKB_RUSSIAN_LOWER:
            case InputModeSwitcher.MODE_SKB_RUSSIAN_UPPER:
                if (keyCode >= RussianKeyCodes.RUSSIAN_F && keyCode <= RussianKeyCodes.RUSSIAN_Z) {
                    keyCodeMapped = keyCode;
                } else {
                    keyCodeMapped = CyrillicKeyMap.getRussianKeys().get(keyCode);
                }
                break;
            case InputModeSwitcher.MODE_SKB_UKRAINIAN_LOWER:
            case InputModeSwitcher.MODE_SKB_UKRAINIAN_UPPER:

                if (keyCode >= RussianKeyCodes.RUSSIAN_F && keyCode <= UkrainianKeyCodes.UKRAINIAN_G) {
                    keyCodeMapped = keyCode;
                } else {
                    keyCodeMapped = CyrillicKeyMap.getUkrainanKeys().get(keyCode);
                }
                break;
        }
        return keyCodeMapped;
    }
 */

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CAPS_LOCK://k1 -keycode115
                return false;
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                if (event.isCtrlPressed()) {
                    return false;
                } else break;
        }
        if (mSkbContainer != null) {
            if (isRepeat) {
                isRepeat = false;
                return true;
            }

            isAfter = true;
            if (isShowInputView) {
                isShowInputView = false;
                return true;
            }
            if (processKey(event, true)) return true;
        }
//        else {
//            return super.onKeyUp(mapkeyForHKB(event), event);
//        }
        return super.onKeyUp(keyCode, event);
    }

    private boolean processKey(KeyEvent event, boolean realAction) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_CAPS_LOCK)
            return true;

        isShowInputView = false;
        if (ImeState.STATE_BYPASS == mImeState) return false;

        int keyCode = event.getKeyCode();
        // SHIFT-SPACE is used to switch between Chinese and English
        // when HKB is on.
        if (KeyEvent.KEYCODE_SPACE == keyCode && event.isShiftPressed()) {
            if (!realAction) return true;

//            updateIcon(mInputModeSwitcher.switchLanguageWithHkb());
            resetToIdleState(false);

            int allMetaState = KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON
                    | KeyEvent.META_ALT_RIGHT_ON | KeyEvent.META_SHIFT_ON
                    | KeyEvent.META_SHIFT_LEFT_ON
                    | KeyEvent.META_SHIFT_RIGHT_ON | KeyEvent.META_SYM_ON;
            getCurrentInputConnection().clearMetaKeyStates(allMetaState);
            return true;
        }

        if (processEnterKeys(keyCode)) {
            Log.d(TAG, "12312 processEnterKeys");
            return true;
        }

        //switch not working on HKB - antonio 19mar2019 is this still mstar patch?
        // If HKB is on to input English, by-pass the key event so that
        // default key listener will handle it.
        if (mInputModeSwitcher.isEnglishWithHkb()) {
            Log.d(TAG, " mstar patch end mInputModeSwitcher.isEnglishWithHkb() return false");
            return false;
        }

        if (processFunctionKeys(keyCode, realAction)) {
            return true;
        }

        if (event.isShiftPressed()) {
            if ((keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT))
                return false;
            if (keyCode != KeyEvent.KEYCODE_6 && (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) ||
                    /* keyCode == KeyEvent.KEYCODE_GRAVE ||*/
                    keyCode == KeyEvent.KEYCODE_MINUS ||
                    keyCode == KeyEvent.KEYCODE_EQUALS ||
                    keyCode == KeyEvent.KEYCODE_LEFT_BRACKET ||
                    keyCode == KeyEvent.KEYCODE_RIGHT_BRACKET ||
                    keyCode == KeyEvent.KEYCODE_BACKSLASH ||
                    keyCode == KeyEvent.KEYCODE_SEMICOLON ||
                    keyCode == KeyEvent.KEYCODE_APOSTROPHE ||
                    keyCode == KeyEvent.KEYCODE_COMMA ||
                    keyCode == KeyEvent.KEYCODE_PERIOD ||
                    keyCode == KeyEvent.KEYCODE_SLASH) {
                return false;
            }
        }
        return inputProcessor.processKey(mInputModeSwitcher.getInputMode(), getCurrentInputConnection(), event,
                event.isShiftPressed() || mInputModeSwitcher.isUpperCaseWithSkb(), realAction, mComposingView);
    }


    /***********************************************************************
     * process direction key event.
     * @param keyCode
     * @return
     ***********************************************************************/
    private boolean processDirectionKeys(int keyCode) {
        if (isAfter) {
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            mSkbContainer.actionForKeyEvent(KEY_LEFT);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            mSkbContainer.actionForKeyEvent(KEY_RIGHT);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (mCandidatesContainer == null) {
                mSkbContainer.setCandViewOnShow(false);
            } else if (!mCandidatesContainer.isShown()) {
                mSkbContainer.setCandViewOnShow(false);
            } else {
                mSkbContainer.setCandViewOnShow(true);
            }
            mSkbContainer.actionForKeyEvent(KEY_UP);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            mSkbContainer.actionForKeyEvent(KEY_DOWN);
            return true;
        }

        return false;
    }

    /***********************************************************************
     * process  softkeyboardview enter key event.
     * @param keyCode  isAccfter
     * @return
     ***********************************************************************/
    private boolean processEnterKeys(int keyCode) {
        if (isInputViewShown() && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
            if (isAfter) {
                mSkbContainer.actionForEnterUp();
                Log.d(TAG, "12312 processEnterKeys true");
                return true;

            } else {
                mSkbContainer.actionForEnterDown();
                if (isSimulateEnterUp) {
                    mSkbContainer.actionForEnterUp();
                }
                Log.d(TAG, "12312 processEnterKeys true2");
                return true;

            }
        }
        Log.d(TAG, "12312 processEnterKeys false");
        return false;
    }

    // keyCode can be from both hard key or soft key.
    private boolean processFunctionKeys(int keyCode, boolean realAction) {
        // Back key is used to dismiss all popup UI in a soft keyboard.
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isInputViewShown() && mSkbContainer != null) {
                if (mSkbContainer.handleBack(realAction)) {
                    return true;
                }
            }
        }
        if (isInputViewShown() && mSkbContainer != null && !skbFocusEnable && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            skbFocusEnable = true;
            mSkbContainer.setInputViewFocusable(true);
            return true;
        }

        if (!isAfter && skbFocusEnable) {
            if (!isInputViewShown()) return false;
            if (processDirectionKeys(keyCode)) return true;
        }

        if (!skbFocusEnable && candFocusEnable) {
            return false;
        }

        if (processEnterKeys(keyCode)) {
            return true;
        }

        if (!skbFocusEnable && candFocusEnable
                && !mDecInfo.isCandidatesListEmpty()) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                if (!realAction) return true;

                chooseCandidate(-1);
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (!realAction) return true;
                mCandidatesContainer.activeCurseBackward();
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (!realAction) return true;
                mCandidatesContainer.activeCurseForward();
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                if (!realAction) return true;
                mCandidatesContainer.pageBackward(false, true);
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (!realAction) return true;
                mCandidatesContainer.pageForward(false, true);
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DEL &&
                    ImeState.STATE_PREDICT == mImeState) {
                if (!realAction) return true;
                resetToIdleState(false);
                return true;
            }
        } else {
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                if (!realAction) {
                    return true;
                }

                if (SIMULATE_KEY_DELETE) {
                    Log.d(TAG, "bbbbbbbb:" + keyCode);
                    simulateKeyEventDownUp(keyCode);
                } else {
                    getCurrentInputConnection().deleteSurroundingText(1, 0);
                }
                return true;
            }
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
                if (!realAction) return true;
                sendKeyChar('\n');
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_SPACE) {
                if (!realAction) return true;
                sendKeyChar(' ');
                return true;
            }
        }

        return false;
    }

    private void changeToStateComposing(boolean updateUi) {
        mImeState = ImeState.STATE_COMPOSING;
        if (!updateUi) return;

        if (null != mSkbContainer && mSkbContainer.isShown()) {
            mSkbContainer.toggleCandidateMode(true);
        }
    }

    private void changeToStateInput(boolean updateUi) {
        mImeState = ImeState.STATE_INPUT;
        if (!updateUi) return;

        if (null != mSkbContainer && mSkbContainer.isShown()) {
            mSkbContainer.toggleCandidateMode(true);
        }
        showCandidateWindow(true);
    }

    private void simulateKeyEventDownUp(int keyCode) {
        InputConnection ic = getCurrentInputConnection();
        if (null == ic) return;

        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
    }

    private void commitResultText(String resultText) {
        InputConnection ic = getCurrentInputConnection();
        if (null != ic) ic.commitText(resultText, 1);
        if (null != mComposingView) {
            mComposingView.setVisibility(View.INVISIBLE);
            mComposingView.invalidate();
        }
    }

    private void updateComposingText(boolean visible) {
        if (!visible) {
            mComposingView.setVisibility(View.INVISIBLE);
        } else {
            mComposingView.setDecodingInfo(mDecInfo, mImeState);
            mComposingView.setVisibility(View.VISIBLE);
        }
        mComposingView.invalidate();
    }

    private void inputCommaPeriod(String preEdit, int keyChar,
                                  boolean dismissCandWindow, ImeState nextState) {
        if (keyChar == ',')
            preEdit += '\uff0c';
        else if (keyChar == '.')
            preEdit += '\u3002';
        else
            return;
        commitResultText(preEdit);
        if (dismissCandWindow) resetCandidateWindow();
        mImeState = nextState;
    }

    private void resetToIdleState(boolean resetInlineText) {
        if (ImeState.STATE_IDLE == mImeState) return;

        mImeState = ImeState.STATE_IDLE;
        mDecInfo.reset();

        if (null != mComposingView) mComposingView.reset();
        if (resetInlineText) commitResultText("");
        resetCandidateWindow();
    }

    private void chooseAndUpdate(int candId) {
//        if (!mInputModeSwitcher.isChineseText()) {
//            String choice = mDecInfo.getCandidate(candId);
//            if (null != choice) {
//                commitResultText(choice);
//            }
//            resetToIdleState(false);
//            return;
//        }

        if (ImeState.STATE_PREDICT != mImeState) {
            // Get result candidate list, if choice_id < 0, do a new decoding.
            // If choice_id >=0, select the candidate, and get the new candidate
            // list.
            mDecInfo.chooseDecodingCandidate(candId);
        } else {
            // Choose a prediction item.
            mDecInfo.choosePredictChoice(candId);
        }

        if (mDecInfo.getComposingStr().length() > 0) {
            String resultStr;
            resultStr = mDecInfo.getComposingStrActivePart();

            // choiceId >= 0 means user finishes a choice selection.
            if (candId >= 0 && mDecInfo.canDoPrediction()) {
                commitResultText(resultStr);
                mImeState = ImeState.STATE_PREDICT;
                if (null != mSkbContainer && mSkbContainer.isShown()) {
                    mSkbContainer.toggleCandidateMode(false);
                }
                // Try to get the prediction list.
                if (Settings.getPrediction()) {
                    InputConnection ic = getCurrentInputConnection();
                    if (null != ic) {
                        CharSequence cs = ic.getTextBeforeCursor(3, 0);
                        if (null != cs) {
                            mDecInfo.preparePredicts(cs);
                        }
                    }
                } else {
                    mDecInfo.resetCandidates();
                }

                if (mDecInfo.mCandidatesList.size() > 0) {
                    showCandidateWindow(false);
                } else {
                    resetToIdleState(false);
                }
            } else {
                if (ImeState.STATE_IDLE == mImeState) {
                    if (mDecInfo.getSplStrDecodedLen() == 0) {
                        changeToStateComposing(true);
                    } else {
                        changeToStateInput(true);
                    }
                } else {
                    if (mDecInfo.selectionFinished()) {
                        changeToStateComposing(true);
                    }
                }
                showCandidateWindow(true);
            }
        } else {
            resetToIdleState(false);
        }
    }

    // If activeCandNo is less than 0, get the current active candidate number
    // from candidate view, otherwise use activeCandNo.
    private void chooseCandidate(int activeCandNo) {
        if (activeCandNo < 0) {
            activeCandNo = mCandidatesContainer.getActiveCandiatePos();
        }
        if (activeCandNo >= 0) {
            chooseAndUpdate(activeCandNo);
        }
    }

    @Override
    public View onCreateCandidatesView() {
        if (mEnvironment.needDebug()) {
            Log.d(TAG, "onCreateCandidatesView.");
        }

        LayoutInflater inflater = getLayoutInflater();
        // Inflate the floating container view
        mFloatingContainer = (LinearLayout) inflater.inflate(
                R.layout.floating_container, null);

        // The first child is the composing view.
        mComposingView = (ComposingView) mFloatingContainer.getChildAt(0);

        mCandidatesContainer = (CandidatesContainer) inflater.inflate(
                R.layout.candidates_container, null);

        // Create balloon hint for candidates view.
        mCandidatesBalloon = new BalloonHint(this, mCandidatesContainer,
                MeasureSpec.UNSPECIFIED);
        mCandidatesBalloon.setBalloonBackground(getResources().getDrawable(
                R.drawable.candidate_balloon_bg));
        mCandidatesContainer.initialize(mChoiceNotifier, mCandidatesBalloon,
                mGestureDetectorCandidates);

        // The floating window
        if (null != mFloatingWindow && mFloatingWindow.isShowing()) {
            mFloatingWindowTimer.cancelShowing();
            mFloatingWindow.dismiss();
        }
        mFloatingWindow = new PopupWindow(this);
        mFloatingWindow.setClippingEnabled(false);
        mFloatingWindow.setBackgroundDrawable(null);
        mFloatingWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        mFloatingWindow.setContentView(mFloatingContainer);

        setCandidatesViewShown(true);
        return mCandidatesContainer;
    }

    public void responseSoftKeyEvent(SoftKey sKey) {
        //Log.d(TAG, "responseSoftKeyEvent:"+sKey.getKeyCode());
        if (null == sKey) return;

        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        int keyCode = sKey.getKeyCode();
        // Process some general keys, including KEYCODE_DEL, KEYCODE_SPACE,
        // KEYCODE_ENTER and KEYCODE_DPAD_CENTER.
        if (sKey.isKeyCodeKey()) {
            if (processFunctionKeys(keyCode, true)) return;
        }

        if (sKey.isUserDefKey()) {
            updateIcon(mInputModeSwitcher.switchModeForUserKey(keyCode));
            resetToIdleState(false);
            mSkbContainer.updateInputMode();
            if (mDecInfo.isCandidatesListEmpty()) {
                dismissCandidateWindow();
            }
        } else {
            if (sKey.isKeyCodeKey()) {
                KeyEvent eDown = new KeyEvent(System.currentTimeMillis(), 0, KeyEvent.ACTION_DOWN,
                        keyCode, 0, 0, 0, 0, KeyEvent.FLAG_SOFT_KEYBOARD);
                KeyEvent eUp = new KeyEvent(System.currentTimeMillis(), 0, KeyEvent.ACTION_UP, keyCode,
                        0, 0, 0, 0, KeyEvent.FLAG_SOFT_KEYBOARD);

                isSimulate = true;
                onKeyDown(keyCode, eDown);
                onKeyUp(keyCode, eUp);
            } else if (sKey.isUniStrKey()) {
                boolean kUsed = false;
                String keyLabel = sKey.getKeyLabel();
//                if (mInputModeSwitcher.isChineseTextWithSkb()
//                        && (ImeState.STATE_INPUT == mImeState || ImeState.STATE_COMPOSING == mImeState)) {
//                    if (mDecInfo.length() > 0 && keyLabel.length() == 1
//                            && keyLabel.charAt(0) == '\'') {
//                        processSurfaceChange('\'', 0);
//                        kUsed = true;
//                    }
//                }
                if (!kUsed) {
                    if (ImeState.STATE_INPUT == mImeState) {
                        commitResultText(mDecInfo
                                .getCurrentFullSent(mCandidatesContainer
                                        .getActiveCandiatePos()));
                    } else if (ImeState.STATE_COMPOSING == mImeState) {
                        commitResultText(mDecInfo.getComposingStr());
                    }
                    commitResultText(keyLabel);
                    resetToIdleState(false);
                }
            }

            // If the current soft keyboard is not sticky, IME needs to go
            // back to the previous soft keyboard automatically.
            if (!mSkbContainer.isCurrentSkbSticky()) {
                updateIcon(mInputModeSwitcher.requestBackToPreviousSkb());
                resetToIdleState(false);
                mSkbContainer.updateInputMode();
            }
        }
    }

    private void showCandidateWindow(boolean showComposingView) {
        if (mEnvironment.needDebug()) {
            Log.d(TAG, "Candidates window is shown. Parent = "
                    + mCandidatesContainer);
        }

        //setCandidatesViewShown(true);

        if (mDecInfo != null && !mDecInfo.isCandidatesListEmpty()) {
            setCandidatesViewShown(true);
        } else {
            setCandidatesViewShown(false);
        }

        if (null != mSkbContainer) mSkbContainer.requestLayout();

        if (null == mCandidatesContainer) {
            resetToIdleState(false);
            return;
        }

        updateComposingText(showComposingView);
        mCandidatesContainer.showCandidates(mDecInfo,
                ImeState.STATE_COMPOSING != mImeState);
        mFloatingWindowTimer.postShowFloatingWindow();
    }

    private void dismissCandidateWindow() {
        if (mEnvironment.needDebug()) {
            Log.d(TAG, "Candidates window is to be dismissed");
        }
        if (null == mCandidatesContainer) return;
        try {
            mFloatingWindowTimer.cancelShowing();
            mFloatingWindow.dismiss();
        } catch (Exception e) {
            Log.e(TAG, "Fail to show the PopupWindow.");
        }
        setCandidatesViewShown(false);

        if (null != mSkbContainer && mSkbContainer.isShown()) {
            mSkbContainer.toggleCandidateMode(false);
        }
    }

    @Override
    public void setCandidatesViewShown(boolean shown) {
        super.setCandidatesViewShown(shown);

    }

    String sysProp = "sys.kiviinputmethod";

    private void setSystemProp(String prop, String value) {
        try {
            Class properties = Class.forName("android.os.SystemProperties");
            Method setProp = properties.getMethod("set",
                    new Class[]{String.class, String.class});
            setProp.invoke(properties, new Object[]{prop, value});
            Log.e("setSystemProp", "ok " + prop + ":" + value);
        } catch (Exception e) {
            Log.e("setSystemProp", "err " + prop + ":" + value + " error " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resetCandidateWindow() {
        if (mEnvironment.needDebug()) {
            Log.d(TAG, "Candidates window is to be reset");
        }
        if (null == mCandidatesContainer) return;
        try {
            mFloatingWindowTimer.cancelShowing();
            mFloatingWindow.dismiss();
        } catch (Exception e) {
            Log.e(TAG, "Fail to show the PopupWindow.");
        }

        if (null != mSkbContainer && mSkbContainer.isShown()) {
            mSkbContainer.toggleCandidateMode(false);
        }

        mDecInfo.resetCandidates();

        if (null != mCandidatesContainer && mCandidatesContainer.isShown()) {
            showCandidateWindow(false);
        }
    }

    private void updateIcon(int iconId) {
        if (iconId > 0) {
            showStatusIcon(iconId);
        } else {
            hideStatusIcon();
        }
    }

    @Override
    public View onCreateInputView() {
        if (mEnvironment.needDebug()) {
            Log.d(TAG, "onCreateInputView.");
        }
        LayoutInflater inflater = getLayoutInflater();
        mSkbContainer = (SkbContainer) inflater.inflate(R.layout.skb_container,
                null);
        mSkbContainer.setService(this);
        mSkbContainer.setInputModeSwitcher(mInputModeSwitcher);
        mSkbContainer.setGestureDetector(mGestureDetectorSkb);
        return mSkbContainer;
    }

    @Override
    public void onStartInput(EditorInfo editorInfo, boolean restarting) {
        if (mEnvironment.needDebug()) {
            Log.d(TAG, "onStartInput " + " ccontentType: "
                    + String.valueOf(editorInfo.inputType) + " Restarting:"
                    + String.valueOf(restarting));
        }
        mInputModeSwitcher.mEditorInfo = editorInfo;
        mInputModeSwitcher.mEditorInfo.imeOptions |= EditorInfo.IME_FLAG_NO_FULLSCREEN;
        resetToIdleState(false);
    }

    @Override
    public void onStartInputView(EditorInfo editorInfo, boolean restarting) {
        if (mEnvironment.needDebug()) {
            Log.d(TAG, "onStartInputView " + " contentType: "
                    + String.valueOf(editorInfo.inputType) + " Restarting:"
                    + String.valueOf(restarting));
        }

        setSystemProp(sysProp, "1");
        KeySelector.INSTANCE.setKeysToSelect(defaultPair);

        updateIcon(mInputModeSwitcher.requestInputWithSkb(editorInfo));
        resetToIdleState(false);
        mSkbContainer.updateInputMode();
        isShowInputView = true;
        setCandidatesViewShown(false);
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        if (mEnvironment.needDebug()) {
            Log.d(TAG, "onFinishInputView.");
        }
        setSystemProp(sysProp, "0");
        KeySelector.INSTANCE.setKeysToSelect(defaultPair);

        mSkbContainer.setInputViewFocusable(true);
        mInputModeSwitcher.resetInputModeToHkbEnglish();
        isShowInputView = false;
        resetToIdleState(false);
        super.onFinishInputView(finishingInput);
    }

    @Override
    public void onFinishInput() {
        if (mEnvironment.needDebug()) {
            Log.d(TAG, "onFinishInput.");
        }
        isShowInputView = false;
        resetToIdleState(false);
        super.onFinishInput();
    }

    @Override
    public void onFinishCandidatesView(boolean finishingInput) {
        if (mEnvironment.needDebug()) {
            Log.d(TAG, "onFinishCandidateView.");
        }
        resetToIdleState(false);
        super.onFinishCandidatesView(finishingInput);
    }

    @Override
    public void onDisplayCompletions(CompletionInfo[] completions) {
        if (!isFullscreenMode()) return;
        if (null == completions || completions.length <= 0) return;
        if (null == mSkbContainer || !mSkbContainer.isShown()) return;

        if (ImeState.STATE_IDLE == mImeState ||
                ImeState.STATE_PREDICT == mImeState) {
            mImeState = ImeState.STATE_APP_COMPLETION;
            mDecInfo.prepareAppCompletions(completions);
            showCandidateWindow(false);
        }
    }

    private void onChoiceTouched(int activeCandNo) {
        if (mImeState == ImeState.STATE_COMPOSING) {
            changeToStateInput(true);
        } else if (mImeState == ImeState.STATE_INPUT
                || mImeState == ImeState.STATE_PREDICT) {
            chooseCandidate(activeCandNo);
        } else if (mImeState == ImeState.STATE_APP_COMPLETION) {
            if (null != mDecInfo.mAppCompletions && activeCandNo >= 0 &&
                    activeCandNo < mDecInfo.mAppCompletions.length) {
                CompletionInfo ci = mDecInfo.mAppCompletions[activeCandNo];
                if (null != ci) {
                    InputConnection ic = getCurrentInputConnection();
                    ic.commitCompletion(ci);
                }
            }
            resetToIdleState(false);
        }
    }

    @Override
    public void requestHideSelf(int flags) {
        if (mEnvironment.needDebug()) {
            Log.d(TAG, "DimissSoftInput.");
        }
        dismissCandidateWindow();
        if (null != mSkbContainer && mSkbContainer.isShown()) {
            mSkbContainer.dismissPopups();
        }
        super.requestHideSelf(flags);
    }

    public void showOptionsMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setIcon(R.drawable.app_icon);
        builder.setNegativeButton(android.R.string.cancel, null);
        CharSequence itemSettings = "Input name";
        CharSequence itemInputMethod = "Input method";
        builder.setItems(new CharSequence[]{itemSettings, itemInputMethod},
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface di, int position) {
                        di.dismiss();
                        switch (position) {
                            case 0:
                                launchSettings();
                                break;
                            case 1:
//                            InputMethodManager.getInstance()
//                                    .showInputMethodPicker();
                                break;
                        }
                    }
                });
        builder.setTitle("Input name");
        mOptionsDialog = builder.create();
        Window window = mOptionsDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.token = mSkbContainer.getWindowToken();
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        mOptionsDialog.show();
    }

    private void launchSettings() {
        Intent intent = new Intent();
        intent.setClass(PinyinIME.this, SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private class PopupTimer extends Handler implements Runnable {
        private int mParentLocation[] = new int[2];

        void postShowFloatingWindow() {
            mFloatingContainer.measure(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            mFloatingWindow.setWidth(mFloatingContainer.getMeasuredWidth());
            mFloatingWindow.setHeight(mFloatingContainer.getMeasuredHeight());
            post(this);
        }

        void cancelShowing() {
            if (mFloatingWindow.isShowing()) {
                mFloatingWindow.dismiss();
            }
            removeCallbacks(this);
        }

        public void run() {
            mCandidatesContainer.getLocationInWindow(mParentLocation);

            if (!mFloatingWindow.isShowing()) {
                mFloatingWindow.showAtLocation(mCandidatesContainer,
                        Gravity.LEFT | Gravity.TOP, mParentLocation[0],
                        mParentLocation[1] - mFloatingWindow.getHeight());
            } else {
                mFloatingWindow
                        .update(mParentLocation[0],
                                mParentLocation[1] - mFloatingWindow.getHeight(),
                                mFloatingWindow.getWidth(),
                                mFloatingWindow.getHeight());
            }
        }
    }

    /**
     * Used to notify IME that the user selects a candidate or performs an
     * gesture.
     */
    public class ChoiceNotifier extends Handler implements
            CandidateViewListener {
        PinyinIME mIme;

        ChoiceNotifier(PinyinIME ime) {
            mIme = ime;
        }

        public void onClickChoice(int choiceId) {
            if (choiceId >= 0) {
                mIme.onChoiceTouched(choiceId);
            }
        }

        public void onToLeftGesture() {
            if (ImeState.STATE_COMPOSING == mImeState) {
                changeToStateInput(true);
            }
            mCandidatesContainer.pageForward(true, false);
        }

        public void onToRightGesture() {
            if (ImeState.STATE_COMPOSING == mImeState) {
                changeToStateInput(true);
            }
            mCandidatesContainer.pageBackward(true, false);
        }

        public void onToTopGesture() {
        }

        public void onToBottomGesture() {
        }
    }

    public class OnGestureListener extends
            GestureDetector.SimpleOnGestureListener {
        /**
         * When user presses and drags, the minimum x-distance to make a
         * response to the drag event.
         */
        private static final int MIN_X_FOR_DRAG = 60;

        /**
         * When user presses and drags, the minimum y-distance to make a
         * response to the drag event.
         */
        private static final int MIN_Y_FOR_DRAG = 40;

        /**
         * Velocity threshold for a screen-move gesture. If the minimum
         * x-velocity is less than it, no gesture.
         */
        static private final float VELOCITY_THRESHOLD_X1 = 0.3f;

        /**
         * Velocity threshold for a screen-move gesture. If the maximum
         * x-velocity is less than it, no gesture.
         */
        static private final float VELOCITY_THRESHOLD_X2 = 0.7f;

        /**
         * Velocity threshold for a screen-move gesture. If the minimum
         * y-velocity is less than it, no gesture.
         */
        static private final float VELOCITY_THRESHOLD_Y1 = 0.2f;

        /**
         * Velocity threshold for a screen-move gesture. If the maximum
         * y-velocity is less than it, no gesture.
         */
        static private final float VELOCITY_THRESHOLD_Y2 = 0.45f;

        /**
         * If it false, we will not response detected gestures.
         */
        private boolean mReponseGestures;

        /**
         * The minimum X velocity observed in the gesture.
         */
        private float mMinVelocityX = Float.MAX_VALUE;

        /**
         * The minimum Y velocity observed in the gesture.
         */
        private float mMinVelocityY = Float.MAX_VALUE;

        /**
         * The first down time for the series of touch events for an action.
         */
        private long mTimeDown;

        /**
         * The last time when onScroll() is called.
         */
        private long mTimeLastOnScroll;

        /**
         * This flag used to indicate that this gesture is not a gesture.
         */
        private boolean mNotGesture;

        /**
         * This flag used to indicate that this gesture has been recognized.
         */
        private boolean mGestureRecognized;

        public OnGestureListener(boolean reponseGestures) {
            mReponseGestures = reponseGestures;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            mMinVelocityX = Integer.MAX_VALUE;
            mMinVelocityY = Integer.MAX_VALUE;
            mTimeDown = e.getEventTime();
            mTimeLastOnScroll = mTimeDown;
            mNotGesture = false;
            mGestureRecognized = false;
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            if (mNotGesture) return false;
            if (mGestureRecognized) return true;

            if (Math.abs(e1.getX() - e2.getX()) < MIN_X_FOR_DRAG
                    && Math.abs(e1.getY() - e2.getY()) < MIN_Y_FOR_DRAG)
                return false;

            long timeNow = e2.getEventTime();
            long spanTotal = timeNow - mTimeDown;
            long spanThis = timeNow - mTimeLastOnScroll;
            if (0 == spanTotal) spanTotal = 1;
            if (0 == spanThis) spanThis = 1;

            float vXTotal = (e2.getX() - e1.getX()) / spanTotal;
            float vYTotal = (e2.getY() - e1.getY()) / spanTotal;

            // The distances are from the current point to the previous one.
            float vXThis = -distanceX / spanThis;
            float vYThis = -distanceY / spanThis;

            float kX = vXTotal * vXThis;
            float kY = vYTotal * vYThis;
            float k1 = kX + kY;
            float k2 = Math.abs(kX) + Math.abs(kY);

            if (k1 / k2 < 0.8) {
                mNotGesture = true;
                return false;
            }
            float absVXTotal = Math.abs(vXTotal);
            float absVYTotal = Math.abs(vYTotal);
            if (absVXTotal < mMinVelocityX) {
                mMinVelocityX = absVXTotal;
            }
            if (absVYTotal < mMinVelocityY) {
                mMinVelocityY = absVYTotal;
            }

            if (mMinVelocityX < VELOCITY_THRESHOLD_X1
                    && mMinVelocityY < VELOCITY_THRESHOLD_Y1) {
                mNotGesture = true;
                return false;
            }

            if (vXTotal > VELOCITY_THRESHOLD_X2
                    && absVYTotal < VELOCITY_THRESHOLD_Y2) {
                if (mReponseGestures) onDirectionGesture(Gravity.RIGHT);
                mGestureRecognized = true;
            } else if (vXTotal < -VELOCITY_THRESHOLD_X2
                    && absVYTotal < VELOCITY_THRESHOLD_Y2) {
                if (mReponseGestures) onDirectionGesture(Gravity.LEFT);
                mGestureRecognized = true;
            } else if (vYTotal > VELOCITY_THRESHOLD_Y2
                    && absVXTotal < VELOCITY_THRESHOLD_X2) {
                if (mReponseGestures) onDirectionGesture(Gravity.BOTTOM);
                mGestureRecognized = true;
            } else if (vYTotal < -VELOCITY_THRESHOLD_Y2
                    && absVXTotal < VELOCITY_THRESHOLD_X2) {
                if (mReponseGestures) onDirectionGesture(Gravity.TOP);
                mGestureRecognized = true;
            }

            mTimeLastOnScroll = timeNow;
            return mGestureRecognized;
        }

        @Override
        public boolean onFling(MotionEvent me1, MotionEvent me2,
                               float velocityX, float velocityY) {
            return mGestureRecognized;
        }

        public void onDirectionGesture(int gravity) {
            if (Gravity.NO_GRAVITY == gravity) {
                return;
            }

            if (Gravity.LEFT == gravity || Gravity.RIGHT == gravity) {
                if (mCandidatesContainer.isShown()) {
                    if (Gravity.LEFT == gravity) {
                        mCandidatesContainer.pageForward(true, true);
                    } else {
                        mCandidatesContainer.pageBackward(true, true);
                    }
                    return;
                }
            }
        }
    }

    public enum ImeState {
        STATE_BYPASS, STATE_IDLE, STATE_INPUT, STATE_COMPOSING, STATE_PREDICT,
        STATE_APP_COMPLETION
    }

    public class DecodingInfo {
        /**
         * Maximum length of the Pinyin string
         */
        private static final int PY_STRING_MAX = 28;

        /**
         * Maximum number of candidates to display in one page.
         */
        private static final int MAX_PAGE_SIZE_DISPLAY = 10;

        /**
         * Spelling (Pinyin) string.
         */
        private StringBuffer mSurface;

        /**
         * Byte buffer used as the Pinyin string parameter for native function
         * call.
         */
        private byte mPyBuf[];

        /**
         * The length of surface string successfully decoded by engine.
         */
        private int mSurfaceDecodedLen;

        /**
         * Composing string.
         */
        private String mComposingStr;

        /**
         * Length of the active composing string.
         */
        private int mActiveCmpsLen;

        /**
         * Composing string for display, it is copied from mComposingStr, and
         * add spaces between spellings.
         **/
        private String mComposingStrDisplay;

        /**
         * Length of the active composing string for display.
         */
        private int mActiveCmpsDisplayLen;

        /**
         * The first full sentence choice.
         */
        private String mFullSent;

        /**
         * Number of characters which have been fixed.
         */
        private int mFixedLen;

        /**
         * If this flag is true, selection is finished.
         */
        private boolean mFinishSelection;

        /**
         * The starting position for each spelling. The first one is the number
         * of the real starting position elements.
         */
        private int mSplStart[];

        /**
         * Editing cursor in mSurface.
         */
        private int mCursorPos;

        /**
         * The complication information suggested by application.
         */
        private CompletionInfo[] mAppCompletions;

        /**
         * The total number of choices for display. The list may only contains
         * the first part. If user tries to navigate to next page which is not
         * in the result list, we need to get these items.
         **/
        public int mTotalChoicesNum;

        /**
         * Candidate list. The first one is the full-sentence candidate.
         */
        public List<String> mCandidatesList = new Vector<String>();

        /**
         * Element i stores the starting position of page i.
         */
        public Vector<Integer> mPageStart = new Vector<Integer>();

        /**
         * Element i stores the number of characters to page i.
         */
        public Vector<Integer> mCnToPage = new Vector<Integer>();

        /**
         * The position to delete in Pinyin string. If it is less than 0, IME
         * will do an incremental search, otherwise IME will do a deletion
         * operation. if {@link #mIsPosInSpl} is true, IME will delete the whole
         * string for mPosDelSpl-th spelling, otherwise it will only delete
         * mPosDelSpl-th character in the Pinyin string.
         */
        public int mPosDelSpl = -1;

        /**
         * If {@link #mPosDelSpl} is big than or equal to 0, this member is used
         * to indicate that whether the postion is counted in spelling id or
         * character.
         */
        public boolean mIsPosInSpl;

        public DecodingInfo() {
            mSurface = new StringBuffer();
            mSurfaceDecodedLen = 0;
        }

        public void reset() {
            mSurface.delete(0, mSurface.length());
            mSurfaceDecodedLen = 0;
            mCursorPos = 0;
            mFullSent = "";
            mFixedLen = 0;
            mFinishSelection = false;
            mComposingStr = "";
            mComposingStrDisplay = "";
            mActiveCmpsLen = 0;
            mActiveCmpsDisplayLen = 0;

            resetCandidates();
        }

        public boolean isCandidatesListEmpty() {
            return mCandidatesList.size() == 0;
        }

        public boolean isSplStrFull() {
            return mSurface.length() >= PY_STRING_MAX - 1;
        }

        public void addSplChar(char ch, boolean reset) {
            if (reset) {
                mSurface.delete(0, mSurface.length());
                mSurfaceDecodedLen = 0;
                mCursorPos = 0;
            }
            mSurface.insert(mCursorPos, ch);
            mCursorPos++;
        }

        // Prepare to delete before cursor. We may delete a spelling char if
        // the cursor is in the range of unfixed part, delete a whole spelling
        // if the cursor in inside the range of the fixed part.
        // This function only marks the position used to delete.
        public void prepareDeleteBeforeCursor() {
            if (mCursorPos > 0) {
                int pos;
                for (pos = 0; pos < mFixedLen; pos++) {
                    if (mSplStart[pos + 2] >= mCursorPos
                            && mSplStart[pos + 1] < mCursorPos) {
                        mPosDelSpl = pos;
                        mCursorPos = mSplStart[pos + 1];
                        mIsPosInSpl = true;
                        break;
                    }
                }
                if (mPosDelSpl < 0) {
                    mPosDelSpl = mCursorPos - 1;
                    mCursorPos--;
                    mIsPosInSpl = false;
                }
            }
        }

        public int length() {
            return mSurface.length();
        }

        public char charAt(int index) {
            return mSurface.charAt(index);
        }

        public StringBuffer getOrigianlSplStr() {
            return mSurface;
        }

        public int getSplStrDecodedLen() {
            return mSurfaceDecodedLen;
        }

        public int[] getSplStart() {
            return mSplStart;
        }

        public String getComposingStr() {
            return mComposingStr;
        }

        public String getComposingStrActivePart() {
            assert (mActiveCmpsLen <= mComposingStr.length());
            return mComposingStr.substring(0, mActiveCmpsLen);
        }

        public int getActiveCmpsLen() {
            return mActiveCmpsLen;
        }

        public String getComposingStrForDisplay() {
            return mComposingStrDisplay;
        }

        public int getActiveCmpsDisplayLen() {
            return mActiveCmpsDisplayLen;
        }

        public String getFullSent() {
            return mFullSent;
        }

        public String getCurrentFullSent(int activeCandPos) {
            try {
                String retStr = mFullSent.substring(0, mFixedLen);
                retStr += mCandidatesList.get(activeCandPos);
                return retStr;
            } catch (Exception e) {
                return "";
            }
        }

        public void resetCandidates() {
            mCandidatesList.clear();
            mTotalChoicesNum = 0;

            mPageStart.clear();
            mPageStart.add(0);
            mCnToPage.clear();
            mCnToPage.add(0);
        }

        public boolean candidatesFromApp() {
            return ImeState.STATE_APP_COMPLETION == mImeState;
        }

        public boolean canDoPrediction() {
            return mComposingStr.length() == mFixedLen;
        }

        public boolean selectionFinished() {
            return mFinishSelection;
        }

        // After the user chooses a candidate, input method will do a
        // re-decoding and give the new candidate list.
        // If candidate id is less than 0, means user is inputting Pinyin,
        // not selecting any choice.
        private void chooseDecodingCandidate(int candId) {
            if (mImeState != ImeState.STATE_PREDICT) {
                resetCandidates();
                int totalChoicesNum = 0;
                {
                    if (candId < 0) {
                        if (length() == 0) {
                            totalChoicesNum = 0;
                        } else {
                            if (mPyBuf == null)
                                mPyBuf = new byte[PY_STRING_MAX];
                            for (int i = 0; i < length(); i++)
                                mPyBuf[i] = (byte) charAt(i);
                            mPyBuf[length()] = 0;

                            if (mPosDelSpl < 0) {
                            } else {
                                boolean clear_fixed_this_step = true;
                                if (ImeState.STATE_COMPOSING == mImeState) {
                                    clear_fixed_this_step = false;
                                }
                                mPosDelSpl = -1;
                            }
                        }
                    } else {
                    }
                }
                updateDecInfoForSearch(totalChoicesNum);
            }
        }

        private void updateDecInfoForSearch(int totalChoicesNum) {
            mTotalChoicesNum = totalChoicesNum;
            if (mTotalChoicesNum < 0) {
                mTotalChoicesNum = 0;
                return;
            }

            try {
                String pyStr = "123";
                assert (mSurfaceDecodedLen <= pyStr.length());


                // Update the surface string to the one kept by engine.
                mSurface.replace(0, mSurface.length(), pyStr);

                if (mCursorPos > mSurface.length())
                    mCursorPos = mSurface.length();
                mComposingStr = mFullSent.substring(0, mFixedLen)
                        + mSurface.substring(mSplStart[mFixedLen + 1]);

                mActiveCmpsLen = mComposingStr.length();
                if (mSurfaceDecodedLen > 0) {
                    mActiveCmpsLen = mActiveCmpsLen
                            - (mSurface.length() - mSurfaceDecodedLen);
                }

                // Prepare the display string.
                if (0 == mSurfaceDecodedLen) {
                    mComposingStrDisplay = mComposingStr;
                    mActiveCmpsDisplayLen = mComposingStr.length();
                } else {
                    mComposingStrDisplay = mFullSent.substring(0, mFixedLen);
                    for (int pos = mFixedLen + 1; pos < mSplStart.length - 1; pos++) {
                        mComposingStrDisplay += mSurface.substring(
                                mSplStart[pos], mSplStart[pos + 1]);
                        if (mSplStart[pos + 1] < mSurfaceDecodedLen) {
                            mComposingStrDisplay += " ";
                        }
                    }
                    mActiveCmpsDisplayLen = mComposingStrDisplay.length();
                    if (mSurfaceDecodedLen < mSurface.length()) {
                        mComposingStrDisplay += mSurface
                                .substring(mSurfaceDecodedLen);
                    }
                }

                mFinishSelection = mSplStart.length == mFixedLen + 2;
            } catch (Exception e) {
                mTotalChoicesNum = 0;
                mComposingStr = "";
            }
            // Prepare page 0.
            if (!mFinishSelection) {
                preparePage(0);
            }
        }

        private void choosePredictChoice(int choiceId) {
            if (ImeState.STATE_PREDICT != mImeState || choiceId < 0
                    || choiceId >= mTotalChoicesNum) {
                return;
            }

            String tmp = mCandidatesList.get(choiceId);

            resetCandidates();

            mCandidatesList.add(tmp);
            mTotalChoicesNum = 1;

            mSurface.replace(0, mSurface.length(), "");
            mCursorPos = 0;
            mFullSent = tmp;
            mFixedLen = tmp.length();
            mComposingStr = mFullSent;
            mActiveCmpsLen = mFixedLen;

            mFinishSelection = true;
        }

        public String getCandidate(int candId) {
            // Only loaded items can be gotten, so we use mCandidatesList.size()
            // instead mTotalChoiceNum.
            if (candId < 0 || candId > mCandidatesList.size()) {
                return null;
            }
            return mCandidatesList.get(candId);
        }

        private void getCandiagtesForCache() {
            int fetchStart = mCandidatesList.size();
            int fetchSize = mTotalChoicesNum - fetchStart;
            if (fetchSize > MAX_PAGE_SIZE_DISPLAY) {
                fetchSize = MAX_PAGE_SIZE_DISPLAY;
            }
            {
                List<String> newList = null;
                if (ImeState.STATE_INPUT == mImeState ||
                        ImeState.STATE_IDLE == mImeState ||
                        ImeState.STATE_COMPOSING == mImeState) {
                } else if (ImeState.STATE_PREDICT == mImeState) {
                } else if (ImeState.STATE_APP_COMPLETION == mImeState) {
                    newList = new ArrayList<String>();
                    if (null != mAppCompletions) {
                        for (int pos = fetchStart; pos < fetchSize; pos++) {
                            CompletionInfo ci = mAppCompletions[pos];
                            if (null != ci) {
                                CharSequence s = ci.getText();
                                if (null != s) newList.add(s.toString());
                            }
                        }
                    }
                }
                mCandidatesList.addAll(newList);
            }
        }

        public boolean pageReady(int pageNo) {
            // If the page number is less than 0, return false
            if (pageNo < 0) return false;

            // Page pageNo's ending information is not ready.
            return mPageStart.size() > pageNo + 1;
        }

        public boolean preparePage(int pageNo) {
            // If the page number is less than 0, return false
            if (pageNo < 0) return false;

            // Make sure the starting information for page pageNo is ready.
            if (mPageStart.size() <= pageNo) {
                return false;
            }

            // Page pageNo's ending information is also ready.
            if (mPageStart.size() > pageNo + 1) {
                return true;
            }

            // If cached items is enough for page pageNo.
            if (mCandidatesList.size() - mPageStart.elementAt(pageNo) >= MAX_PAGE_SIZE_DISPLAY) {
                return true;
            }

            // Try to get more items from engine
            getCandiagtesForCache();

            // Try to find if there are available new items to display.
            // If no new item, return false;
            return mPageStart.elementAt(pageNo) < mCandidatesList.size();
        }

        public void preparePredicts(CharSequence history) {
            if (null == history) return;

            resetCandidates();

            if (Settings.getPrediction()) {
                String preEdit = history.toString();
                int predictNum = 0;
                if (null != preEdit) {
                }
            }

            preparePage(0);
            mFinishSelection = false;
        }

        private void prepareAppCompletions(CompletionInfo completions[]) {
            resetCandidates();
            mAppCompletions = completions;
            mTotalChoicesNum = completions.length;
            preparePage(0);
            mFinishSelection = false;
            return;
        }

        public int getCurrentPageSize(int currentPage) {
            if (mPageStart.size() <= currentPage + 1) return 0;
            return mPageStart.elementAt(currentPage + 1)
                    - mPageStart.elementAt(currentPage);
        }

        public int getCurrentPageStart(int currentPage) {
            if (mPageStart.size() < currentPage + 1) return mTotalChoicesNum;
            return mPageStart.elementAt(currentPage);
        }

        public boolean pageForwardable(int currentPage) {
            if (mPageStart.size() <= currentPage + 1) return false;
            return mPageStart.elementAt(currentPage + 1) < mTotalChoicesNum;
        }

        public boolean pageBackwardable(int currentPage) {
            return currentPage > 0;
        }

        public boolean charBeforeCursorIsSeparator() {
            int len = mSurface.length();
            if (mCursorPos > len) return false;
            return mCursorPos > 0 && mSurface.charAt(mCursorPos - 1) == '\'';
        }

        public int getCursorPos() {
            return mCursorPos;
        }

        public int getCursorPosInCmps() {
            int cursorPos = mCursorPos;
            int fixedLen = 0;

            for (int hzPos = 0; hzPos < mFixedLen; hzPos++) {
                if (mCursorPos >= mSplStart[hzPos + 2]) {
                    cursorPos -= mSplStart[hzPos + 2] - mSplStart[hzPos + 1];
                    cursorPos += 1;
                }
            }
            return cursorPos;
        }

        public int getCursorPosInCmpsDisplay() {
            int cursorPos = getCursorPosInCmps();
            // +2 is because: one for mSplStart[0], which is used for other
            // purpose(The length of the segmentation string), and another
            // for the first spelling which does not need a space before it.
            for (int pos = mFixedLen + 2; pos < mSplStart.length - 1; pos++) {
                if (mCursorPos <= mSplStart[pos]) {
                    break;
                } else {
                    cursorPos++;
                }
            }
            return cursorPos;
        }

        public void moveCursorToEdge(boolean left) {
            if (left)
                mCursorPos = 0;
            else
                mCursorPos = mSurface.length();
        }

        // Move cursor. If offset is 0, this function can be used to adjust
        // the cursor into the bounds of the string.
        public void moveCursor(int offset) {
            if (offset > 1 || offset < -1) return;

            if (offset != 0) {
                int hzPos = 0;
                for (hzPos = 0; hzPos <= mFixedLen; hzPos++) {
                    if (mCursorPos == mSplStart[hzPos + 1]) {
                        if (offset < 0) {
                            if (hzPos > 0) {
                                offset = mSplStart[hzPos]
                                        - mSplStart[hzPos + 1];
                            }
                        } else {
                            if (hzPos < mFixedLen) {
                                offset = mSplStart[hzPos + 2]
                                        - mSplStart[hzPos + 1];
                            }
                        }
                        break;
                    }
                }
            }
            mCursorPos += offset;
            if (mCursorPos < 0) {
                mCursorPos = 0;
            } else if (mCursorPos > mSurface.length()) {
                mCursorPos = mSurface.length();
            }
        }

        public int getSplNum() {
            return mSplStart[0];
        }

        public int getFixedLen() {
            return mFixedLen;
        }
    }
}
