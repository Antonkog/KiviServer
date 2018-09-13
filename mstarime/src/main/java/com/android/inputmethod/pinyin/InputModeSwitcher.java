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

import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;
import android.view.inputmethod.EditorInfo;

import com.android.inputmethod.pinyin.SoftKeyboard.KeyRow;

import java.util.Arrays;
import java.util.List;

/**
 * Switcher used to switching input mode between Chinese, English, symbol,etc.
 */
public class InputModeSwitcher {
    /**
     * User defined key code, used by soft keyboard.
     */
    private static final int USERDEF_KEYCODE_SHIFT_1 = -1;

    /**
     * User defined key code, used by soft keyboard.
     */
    private static final int USERDEF_KEYCODE_LANG_2 = -2;

    /**
     * User defined key code, used by soft keyboard.
     */
    private static final int USERDEF_KEYCODE_SYM_3 = -3;

    /**
     * User defined key code, used by soft keyboard.
     */
    public static final int USERDEF_KEYCODE_PHONE_SYM_4 = -4;

    /**
     * User defined key code, used by soft keyboard.
     */
    private static final int USERDEF_KEYCODE_MORE_SYM_5 = -5;

    /**
     * Bits used to indicate soft keyboard layout. If none bit is set, the
     * current input mode does not require a soft keyboard.
     **/
    private static final int MASK_SKB_LAYOUT = 0xf0000000;

    /**
     * A kind of soft keyboard layout. An input mode should be anded with
     * {@link #MASK_SKB_LAYOUT} to get its soft keyboard layout.
     */
    private static final int MASK_SKB_LAYOUT_QWERTY = 0x10000000;

    /**
     * A kind of soft keyboard layout. An input mode should be anded with
     * {@link #MASK_SKB_LAYOUT} to get its soft keyboard layout.
     */
    private static final int MASK_SKB_LAYOUT_QWERTY_RU = 0x60000000;

    /**
     * A kind of soft keyboard layout. An input mode should be anded with
     * {@link #MASK_SKB_LAYOUT} to get its soft keyboard layout.
     */
    private static final int MASK_SKB_LAYOUT_QWERTY_UA = 0x70000000;

    /**
     * A kind of soft keyboard layout. An input mode should be anded with
     * {@link #MASK_SKB_LAYOUT} to get its soft keyboard layout.
     */
    private static final int MASK_SKB_LAYOUT_SYMBOL1 = 0x20000000;

    /**
     * A kind of soft keyboard layout. An input mode should be anded with
     * {@link #MASK_SKB_LAYOUT} to get its soft keyboard layout.
     */
    private static final int MASK_SKB_LAYOUT_SYMBOL2 = 0x30000000;

    /**
     * A kind of soft keyboard layout. An input mode should be anded with
     * {@link #MASK_SKB_LAYOUT} to get its soft keyboard layout.
     */
    private static final int MASK_SKB_LAYOUT_SMILEY = 0x40000000;

    /**
     * A kind of soft keyboard layout. An input mode should be anded with
     * {@link #MASK_SKB_LAYOUT} to get its soft keyboard layout.
     */
    private static final int MASK_SKB_LAYOUT_PHONE = 0x50000000;

    /**
     * Used to indicate which language the current input mode is in. If the
     * current input mode works with a none-QWERTY soft keyboard, these bits are
     * also used to get language information. For example, a Chinese symbol soft
     * keyboard and an English one are different in an icon which is used to
     * tell user the language information. BTW, the smiley soft keyboard mode
     * shouldn't be set with {@link #MASK_LANGUAGE_EN} because it can only be
     * launched from Chinese QWERTY soft keyboard, and it has Chinese icon on
     * soft keyboard.
     */
    private static final int MASK_LANGUAGE = 0x0f000000;

//    /**
//     * Used to indicate the current language. An input mode should be anded with
//     * {@link #MASK_LANGUAGE} to get this information.
//     */
//    private static final int MASK_LANGUAGE_CN = 0x01000000;

    /**
     * Used to indicate the current language. An input mode should be anded with
     * {@link #MASK_LANGUAGE} to get this information.
     */
    private static final int MASK_LANGUAGE_EN = 0x02000000;

    /**
     * Used to indicate the current language. An input mode should be anded with
     * {@link #MASK_LANGUAGE} to get this information.
     */

    private static final int MASK_LANGUAGE_RU = 0x03000000;

    /**
     * Used to indicate the current language. An input mode should be anded with
     * {@link #MASK_LANGUAGE} to get this information.
     */
    private static final int MASK_LANGUAGE_UA = 0x04000000;


    /**
     * Used to indicate which case the current input mode is in. For example,
     * English QWERTY has lowercase and uppercase. For the Chinese QWERTY, these
     * bits are ignored. For phone keyboard layout, these bits can be
     * {@link #MASK_CASE_UPPER} to request symbol page for phone soft keyboard.
     */
    private static final int MASK_CASE = 0x00f00000;

    /**
     * Used to indicate the current case information. An input mode should be
     * anded with {@link #MASK_CASE} to get this information.
     */
    private static final int MASK_CASE_LOWER = 0x00100000;

    /**
     * Used to indicate the current case information. An input mode should be
     * anded with {@link #MASK_CASE} to get this information.
     */
    private static final int MASK_CASE_UPPER = 0x00200000;

//    /**
//     * Mode for inputing Chinese with soft keyboard.
//     */
//    public static final int MODE_SKB_CHINESE = (MASK_SKB_LAYOUT_QWERTY | MASK_LANGUAGE_CN);

//    /**
//     * Mode for inputing basic symbols for Chinese mode with soft keyboard.
//     */
//    public static final int MODE_SKB_SYMBOL1_CN = (MASK_SKB_LAYOUT_SYMBOL1 | MASK_LANGUAGE_CN);
//
//    /**
//     * Mode for inputing more symbols for Chinese mode with soft keyboard.
//     */
//    public static final int MODE_SKB_SYMBOL2_CN = (MASK_SKB_LAYOUT_SYMBOL2 | MASK_LANGUAGE_CN);

    /**
     * Mode for inputing English lower characters with soft keyboard.
     */
    public static final int MODE_SKB_ENGLISH_LOWER = (MASK_SKB_LAYOUT_QWERTY
            | MASK_LANGUAGE_EN | MASK_CASE_LOWER);

    /**
     * Mode for inputing English upper characters with soft keyboard.
     */
    public static final int MODE_SKB_ENGLISH_UPPER = (MASK_SKB_LAYOUT_QWERTY
            | MASK_LANGUAGE_EN | MASK_CASE_UPPER);
    /**
     * Mode for inputing English lower characters with soft keyboard.
     */
    public static final int MODE_SKB_RUSSIAN_LOWER = (MASK_SKB_LAYOUT_QWERTY_RU
            | MASK_LANGUAGE_RU | MASK_CASE_LOWER);

    /**
     * Mode for inputing English upper characters with soft keyboard.
     */
    public static final int MODE_SKB_RUSSIAN_UPPER = (MASK_SKB_LAYOUT_QWERTY_RU
            | MASK_LANGUAGE_RU | MASK_CASE_UPPER);
    /**
     * Mode for inputing English lower characters with soft keyboard.
     */
    public static final int MODE_SKB_UKRAINIAN_LOWER = (MASK_SKB_LAYOUT_QWERTY_UA
            | MASK_LANGUAGE_UA | MASK_CASE_LOWER);

    /**
     * Mode for inputing English upper characters with soft keyboard.
     */
    public static final int MODE_SKB_UKRAINIAN_UPPER = (MASK_SKB_LAYOUT_QWERTY_UA
            | MASK_LANGUAGE_UA | MASK_CASE_UPPER);

    /**
     * Mode for inputing basic symbols for English mode with soft keyboard.
     */
    public static final int MODE_SKB_SYMBOL1_EN = (MASK_SKB_LAYOUT_SYMBOL1 | MASK_LANGUAGE_EN);

    /**
     * Mode for inputing more symbols for English mode with soft keyboard.
     */
    public static final int MODE_SKB_SYMBOL2_EN = (MASK_SKB_LAYOUT_SYMBOL2 | MASK_LANGUAGE_EN);

//    /**
//     * Mode for inputing smileys with soft keyboard.
//     */
//    public static final int MODE_SKB_SMILEY = (MASK_SKB_LAYOUT_SMILEY | MASK_LANGUAGE_CN);

    /**
     * Mode for inputing phone numbers.
     */
    public static final int MODE_SKB_PHONE_NUM = (MASK_SKB_LAYOUT_PHONE);

    /**
     * Mode for inputing phone numbers.
     */
    public static final int MODE_SKB_PHONE_SYM = (MASK_SKB_LAYOUT_PHONE | MASK_CASE_UPPER);

//    /**
//     * Mode for inputing Chinese with a hardware keyboard.
//     */
//    public static final int MODE_HKB_CHINESE = (MASK_LANGUAGE_CN);

    /**
     * Mode for inputing English with a hardware keyboard
     */
    public static final int MODE_HKB_ENGLISH = (MASK_LANGUAGE_EN);

    /**
     * Unset mode.
     */
    public static final int MODE_UNSET = 0;

    /**
     * Maximum toggle states for a soft keyboard.
     */
    public static final int MAX_TOGGLE_STATES = 4;

    /**
     * The input mode for the current edit box.
     */
    private int mInputMode = MODE_UNSET;

    /**
     * Used to remember previous input mode. When user enters an edit field, the
     * previous input mode will be tried. If the previous mode can not be used
     * for the current situation (For example, previous mode is a soft keyboard
     * mode to input symbols, and we have a hardware keyboard for the current
     * situation), {@link #mRecentLanguageInputMode} will be tried.
     **/
    private int mPreviousInputMode = MODE_SKB_ENGLISH_LOWER;

    /**
     * Used to remember recent mode to input language.
     */
    private int mRecentLanguageInputMode = MODE_SKB_ENGLISH_LOWER;

    /**
     * Editor information of the current edit box.
     */
    public EditorInfo mEditorInfo;

    /**
     * Used to indicate required toggling operations.
     */
    private ToggleStates mToggleStates = new ToggleStates();

    /**
     * The current field is a short message field?
     */
    private boolean mShortMessageField;

    /**
     * Is return key in normal state?
     */
    private boolean mEnterKeyNormal = true;

    /**
     * Current icon. 0 for none icon.
     */
    int mInputIcon = R.drawable.ime_pinyin;

    /**
     * IME service.
     */
    private PinyinIME mImeService;

    /**
     * Key toggling state for English lowwercase mode.
     */
    private int mToggleStateEnLower;

    /**
     * Key toggling state for English upppercase mode.
     */
    private int mToggleStateEnUpper;

    /**
     * Key toggling state for English symbol mode for the first page.
     */
    private int mToggleStateEnSym1;

    /**
     * Key toggling state for English symbol mode for the second page.
     */
    private int mToggleStateEnSym2;


    /**
     * Key toggling state for phone symbol mode.
     */
    private int mToggleStatePhoneSym;

    /**
     * Key toggling state for GO action of ENTER key.
     */
    private int mToggleStateGo;

    /**
     * Key toggling state for SEARCH action of ENTER key.
     */
    private int mToggleStateSearch;

    /**
     * Key toggling state for SEND action of ENTER key.
     */
    private int mToggleStateSend;

    /**
     * Key toggling state for NEXT action of ENTER key.
     */
    private int mToggleStateNext;

    /**
     * Key toggling state for SEND action of ENTER key.
     */
    private int mToggleStateDone;

    /**
     * QWERTY row toggling state for English input.
     */
    private int mToggleRowEn;

    /**
     * QWERTY row toggling state for URI input.
     */
    private int mToggleRowUri;

    /**
     * QWERTY row toggling state for email address input.
     */
    private int mToggleRowEmailAddress;

    class ToggleStates {
        /**
         * If it is true, this soft keyboard is a QWERTY one.
         */
        boolean mQwerty;

        /**
         * If {@link #mQwerty} is true, this variable is used to decide the
         * letter case of the QWERTY keyboard.
         */
        boolean mQwertyUpperCase;

        /**
         * The id of enabled row in the soft keyboard. Refer to
         * {@link com.android.inputmethod.pinyin.SoftKeyboard.KeyRow} for
         * details.
         */
        public int mRowIdToEnable;

        /**
         * Used to store all other toggle states for the current input mode.
         */
        public int mKeyStates[] = new int[MAX_TOGGLE_STATES];

        /**
         * Number of states to toggle.
         */
        public int mKeyStatesNum;
    }

    public InputModeSwitcher(PinyinIME imeService) {
        mImeService = imeService;
        //initCountry();
        Resources r = mImeService.getResources();
        mToggleStateEnLower = Integer.parseInt(r
                .getString(R.string.toggle_en_lower));
        mToggleStateEnUpper = Integer.parseInt(r
                .getString(R.string.toggle_en_upper));
        mToggleStateEnSym1 = Integer.parseInt(r
                .getString(R.string.toggle_en_sym1));
        mToggleStateEnSym2 = Integer.parseInt(r
                .getString(R.string.toggle_en_sym2));
        mToggleStatePhoneSym = Integer.parseInt(r
                .getString(R.string.toggle_phone_sym));

        mToggleStateGo = Integer
                .parseInt(r.getString(R.string.toggle_enter_go));
        mToggleStateSearch = Integer.parseInt(r
                .getString(R.string.toggle_enter_search));
        mToggleStateSend = Integer.parseInt(r
                .getString(R.string.toggle_enter_send));
        mToggleStateNext = Integer.parseInt(r
                .getString(R.string.toggle_enter_next));
        mToggleStateDone = Integer.parseInt(r
                .getString(R.string.toggle_enter_done));

        mToggleRowEn = Integer.parseInt(r.getString(R.string.toggle_row_en));
        mToggleRowUri = Integer.parseInt(r.getString(R.string.toggle_row_uri));
        mToggleRowEmailAddress = Integer.parseInt(r
                .getString(R.string.toggle_row_emailaddress));
    }

    private void initCountry() {
        String Country = null;
        int Index = 51;
        SQLiteDatabase db = SQLiteDatabase.openDatabase("/system/model/model.db",
                null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = db.rawQuery("select * from  build_info where device_model=?", new String[]{android.os.Build.MODEL});

        if (cursor.moveToFirst()) {
            int id = cursor.getColumnIndex("country");
            Country = cursor.getString(id);
            cursor.close();
            currentCountry = CountryLanguage.getCountryByCode(Country);
        }
    }

    public int getInputMode() {
        return mInputMode;
    }

    public ToggleStates getToggleStates() {
        return mToggleStates;
    }

    public int getSkbLayout() {
        int layout = (mInputMode & MASK_SKB_LAYOUT);

        switch (layout) {
            case MASK_SKB_LAYOUT_QWERTY:
                return R.xml.skb_qwerty_en;
            case MASK_SKB_LAYOUT_SYMBOL1:
                return R.xml.skb_sym1;
            case MASK_SKB_LAYOUT_SYMBOL2:
                return R.xml.skb_sym2;
            case MASK_SKB_LAYOUT_SMILEY:
                return R.xml.skb_smiley;
            case MASK_SKB_LAYOUT_PHONE:
                return R.xml.skb_phone;
            case MASK_SKB_LAYOUT_QWERTY_RU:
                return R.xml.skb_qwerty_ru;
            case MASK_SKB_LAYOUT_QWERTY_UA:
                return R.xml.skb_qwerty_ua;
        }
        return 0;
    }

    // Return the icon to update.
    public int switchModeForUserKey(int userKey) {
        int newInputMode = MODE_UNSET;

        if (USERDEF_KEYCODE_LANG_2 == userKey) {
            KeySelector.INSTANCE.setKeysToSelect(new Pair<>(3, 1));
            if (MODE_SKB_ENGLISH_UPPER == mInputMode
                    || MODE_SKB_ENGLISH_LOWER == mInputMode) {
                //  newInputMode = MODE_SKB_RUSSIAN_LOWER;
                newInputMode = currentCountry.getNextLanguage(MODE_SKB_ENGLISH_LOWER);
            }

            // Set this to MODE_SKB_ENGLISH_LOWER
            // to lock out Ukrainian input
            if (MODE_SKB_RUSSIAN_UPPER == mInputMode
                    || MODE_SKB_RUSSIAN_LOWER == mInputMode) {
                //newInputMode = MODE_SKB_UKRAINIAN_LOWER;
                newInputMode = currentCountry.getNextLanguage(MODE_SKB_RUSSIAN_LOWER);
            }

            if (MODE_SKB_UKRAINIAN_LOWER == mInputMode
                    || MODE_SKB_UKRAINIAN_UPPER == mInputMode) {
                //newInputMode = MODE_SKB_ENGLISH_LOWER;
                newInputMode = currentCountry.getNextLanguage(MODE_SKB_UKRAINIAN_LOWER);
            }
        } else if (USERDEF_KEYCODE_SYM_3 == userKey) {
            if (MODE_SKB_ENGLISH_UPPER == mInputMode
                    || MODE_SKB_ENGLISH_LOWER == mInputMode
                    || MODE_SKB_RUSSIAN_LOWER == mInputMode
                    || MODE_SKB_RUSSIAN_UPPER == mInputMode
                    || MODE_SKB_UKRAINIAN_LOWER == mInputMode
                    || MODE_SKB_UKRAINIAN_UPPER == mInputMode) {
                newInputMode = MODE_SKB_SYMBOL1_EN;
            } else if (MODE_SKB_SYMBOL1_EN == mInputMode
                    || MODE_SKB_SYMBOL2_EN == mInputMode) {
                newInputMode = mRecentLanguageInputMode;
            }
        } else if (USERDEF_KEYCODE_SHIFT_1 == userKey) {
            if (MODE_SKB_ENGLISH_LOWER == mInputMode) {
                newInputMode = MODE_SKB_ENGLISH_UPPER;
            } else if (MODE_SKB_ENGLISH_UPPER == mInputMode) {
                newInputMode = MODE_SKB_ENGLISH_LOWER;
            }

            if (MODE_SKB_RUSSIAN_LOWER == mInputMode) {
                newInputMode = MODE_SKB_RUSSIAN_UPPER;
            } else if (MODE_SKB_RUSSIAN_UPPER == mInputMode) {
                newInputMode = MODE_SKB_RUSSIAN_LOWER;
            }

            if (MODE_SKB_UKRAINIAN_LOWER == mInputMode) {
                newInputMode = MODE_SKB_UKRAINIAN_UPPER;
            } else if (MODE_SKB_UKRAINIAN_UPPER == mInputMode) {
                newInputMode = MODE_SKB_UKRAINIAN_LOWER;
            }
        } else if (USERDEF_KEYCODE_MORE_SYM_5 == userKey) {
            int sym = (MASK_SKB_LAYOUT & mInputMode);
            if (MASK_SKB_LAYOUT_SYMBOL1 == sym) {
                sym = MASK_SKB_LAYOUT_SYMBOL2;
            } else {
                sym = MASK_SKB_LAYOUT_SYMBOL1;
            }
            newInputMode = ((mInputMode & (~MASK_SKB_LAYOUT)) | sym);
        } else if (USERDEF_KEYCODE_PHONE_SYM_4 == userKey) {
            if (MODE_SKB_PHONE_NUM == mInputMode) {
                newInputMode = MODE_SKB_PHONE_SYM;
            } else {
                newInputMode = MODE_SKB_PHONE_NUM;
            }
        }

        if (newInputMode == mInputMode || MODE_UNSET == newInputMode) {
            return mInputIcon;
        }

        saveInputMode(newInputMode);
        prepareToggleStates(true);
        return mInputIcon;
    }

    public void switchLanguage() {
        int newInputMode = MODE_UNSET;

        if (MODE_SKB_ENGLISH_UPPER == mInputMode
                || MODE_SKB_ENGLISH_LOWER == mInputMode) {
            newInputMode = currentCountry.getNextLanguage(MODE_SKB_ENGLISH_LOWER);
            //newInputMode = MODE_SKB_RUSSIAN_LOWER;
        }

        if (MODE_SKB_RUSSIAN_UPPER == mInputMode
                || MODE_SKB_RUSSIAN_LOWER == mInputMode) {
            // Set this to MODE_SKB_ENGLISH_LOWER
            // to lock out Ukrainian input
            //newInputMode = MODE_SKB_UKRAINIAN_LOWER;
            newInputMode = currentCountry.getNextLanguage(MODE_SKB_RUSSIAN_LOWER);
        }

        if (MODE_SKB_UKRAINIAN_LOWER == mInputMode
                || MODE_SKB_UKRAINIAN_UPPER == mInputMode) {
            //  newInputMode = MODE_SKB_ENGLISH_LOWER;
            newInputMode = currentCountry.getNextLanguage(MODE_SKB_UKRAINIAN_LOWER);
        }

        saveInputMode(newInputMode);
        prepareToggleStates(true);
    }

    // Return the icon to update.
    public int requestInputWithHkb(EditorInfo editorInfo) {
        mShortMessageField = false;
        boolean english = false;
        int newInputMode = MODE_HKB_ENGLISH;

        switch (editorInfo.inputType & EditorInfo.TYPE_MASK_CLASS) {
            case EditorInfo.TYPE_CLASS_NUMBER:
            case EditorInfo.TYPE_CLASS_PHONE:
            case EditorInfo.TYPE_CLASS_DATETIME:
                english = true;
                break;
            case EditorInfo.TYPE_CLASS_TEXT:
                int v = editorInfo.inputType & EditorInfo.TYPE_MASK_VARIATION;
                if (v == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        || v == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
                        || v == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        || v == EditorInfo.TYPE_TEXT_VARIATION_URI) {
                    english = true;
                } else if (v == EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE) {
                    mShortMessageField = true;
                }
                break;
            default:
        }


        // MStar Android Patch End
        saveInputMode(newInputMode);
        prepareToggleStates(false);
        return mInputIcon;
    }

    // Return the icon to update.
    public int requestInputWithSkb(EditorInfo editorInfo) {
        mShortMessageField = false;
        int newInputMode;

        switch (editorInfo.inputType & EditorInfo.TYPE_MASK_CLASS) {
            case EditorInfo.TYPE_CLASS_NUMBER:
            case EditorInfo.TYPE_CLASS_DATETIME:
                newInputMode = MODE_SKB_SYMBOL1_EN;
                break;
            case EditorInfo.TYPE_CLASS_PHONE:
                newInputMode = MODE_SKB_PHONE_NUM;
                break;
            case EditorInfo.TYPE_CLASS_TEXT:
                int v = editorInfo.inputType & EditorInfo.TYPE_MASK_VARIATION;
                if (v == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        || v == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
                        || v == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        || v == EditorInfo.TYPE_TEXT_VARIATION_URI) {
                    // If the application request English mode, we switch to it.
                    newInputMode = MODE_SKB_ENGLISH_LOWER;
                } else {
                    if (v == EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE) {
                        mShortMessageField = true;
                    }
                    // If the application do not request English mode, we will
                    // try to keep the previous mode.
                    int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
                    newInputMode = mInputMode;
                    if (0 == skbLayout) {
                        if ((mInputMode & MASK_LANGUAGE) == MASK_LANGUAGE_RU) {
                            newInputMode = MODE_SKB_RUSSIAN_LOWER;
                        } else {
                            newInputMode = MODE_SKB_ENGLISH_LOWER;
                        }
                    }
                }
                break;
            default:
                // Try to keep the previous mode.
                int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
                newInputMode = mInputMode;
                if (0 == skbLayout) {
                    if ((mInputMode & MASK_LANGUAGE) == MASK_LANGUAGE_RU) {
                        newInputMode = MODE_SKB_RUSSIAN_LOWER;
                    } else {
                        newInputMode = MODE_SKB_ENGLISH_LOWER;
                    }
                }
                break;
        }

        mEditorInfo = editorInfo;
        mEditorInfo.imeOptions |= EditorInfo.IME_FLAG_NO_FULLSCREEN;

        saveInputMode(newInputMode);
        prepareToggleStates(true);
        return mInputIcon;
    }

    // Return the icon to update.

    public int requestBackToPreviousSkb() {
        int layout = (mInputMode & MASK_SKB_LAYOUT);
        int lastLayout = (mPreviousInputMode & MASK_SKB_LAYOUT);
        if (0 != layout && 0 != lastLayout) {
            mInputMode = mPreviousInputMode;
            saveInputMode(mInputMode);
            prepareToggleStates(true);
            return mInputIcon;
        }
        return 0;
    }

    public boolean isEnglishWithHkb() {
        return MODE_HKB_ENGLISH == mInputMode;
    }

    public boolean isEnglishWithSkb() {
        return MODE_SKB_ENGLISH_LOWER == mInputMode
                || MODE_SKB_ENGLISH_UPPER == mInputMode;
    }

    public boolean isRussianWithSkb() {
        return MODE_SKB_RUSSIAN_LOWER == mInputMode || MODE_SKB_RUSSIAN_UPPER == mInputMode;
    }

    boolean isUkrainianWithSkb() {
        return MODE_SKB_UKRAINIAN_LOWER == mInputMode || MODE_SKB_UKRAINIAN_UPPER == mInputMode;
    }

    public boolean isEnglishUpperCaseWithSkb() {
        return MODE_SKB_ENGLISH_UPPER == mInputMode;
    }

    public boolean isRussianText() {
        int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
        if (MASK_SKB_LAYOUT_QWERTY == skbLayout || 0 == skbLayout) {
            int language = (mInputMode & MASK_LANGUAGE);
            if (MASK_LANGUAGE_RU == language) return true;
        }
        return false;
    }

    boolean isRussianUpperCaseWithSkb() {
        return MODE_SKB_RUSSIAN_UPPER == mInputMode;
    }

    boolean isUkrainianUpperCaseWithSkb() {
        return MODE_SKB_UKRAINIAN_UPPER == mInputMode;
    }

//    public boolean isChineseText() {
//        int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
//        if (MASK_SKB_LAYOUT_QWERTY == skbLayout || 0 == skbLayout) {
//            int language = (mInputMode & MASK_LANGUAGE);
//            if (MASK_LANGUAGE_CN == language) return true;
//        }
//        return false;
//    }
//
//    public boolean isChineseTextWithHkb() {
//        int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
//        if (0 == skbLayout) {
//            int language = (mInputMode & MASK_LANGUAGE);
//            if (MASK_LANGUAGE_CN == language) return true;
//        }
//        return false;
//    }
//
//    public boolean isChineseTextWithSkb() {
//        int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
//        if (MASK_SKB_LAYOUT_QWERTY == skbLayout) {
//            int language = (mInputMode & MASK_LANGUAGE);
//            if (MASK_LANGUAGE_CN == language) return true;
//        }
//        return false;
//    }

    public boolean isSymbolWithSkb() {
        int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
        if (MASK_SKB_LAYOUT_SYMBOL1 == skbLayout
                || MASK_SKB_LAYOUT_SYMBOL2 == skbLayout) {
            return true;
        }
        return false;
    }

    public boolean isEnterNoramlState() {
        return mEnterKeyNormal;
    }

    public boolean tryHandleLongPressSwitch(int keyCode) {
        if (USERDEF_KEYCODE_LANG_2 == keyCode
                || USERDEF_KEYCODE_PHONE_SYM_4 == keyCode) {
//            mImeService.showOptionsMenu();
            return true;
        }
        return false;
    }

    // MStar Android Patch Begin
    public void resetInputModeToHkbEnglish() {
        saveInputMode(MODE_HKB_ENGLISH);
    }
    // MStar Android Patch End

    private void saveInputMode(int newInputMode) {
        mPreviousInputMode = mInputMode;
        mInputMode = newInputMode;

        int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
        if ((MASK_SKB_LAYOUT_QWERTY == skbLayout
                || MASK_SKB_LAYOUT_QWERTY_RU == skbLayout
                || MASK_SKB_LAYOUT_QWERTY_UA == skbLayout) || 0 == skbLayout) {
            mRecentLanguageInputMode = mInputMode;
        }

        mInputIcon = R.drawable.ime_en;
        if (isEnglishWithHkb()) {
            mInputIcon = R.drawable.ime_en;
//        } else if (isChineseTextWithHkb()) {
//            mInputIcon = R.drawable.ime_pinyin;
//        }

            if (!Environment.getInstance().hasHardKeyboard()) {
                mInputIcon = 0;
            }
        }
    }


    private void prepareToggleStates(boolean needSkb) {
        mEnterKeyNormal = true;
        if (!needSkb) return;

        mToggleStates.mQwerty = false;
        mToggleStates.mKeyStatesNum = 0;

        int states[] = mToggleStates.mKeyStates;
        int statesNum = 0;
        // Toggle state for language.
        int language = (mInputMode & MASK_LANGUAGE);
        int layout = (mInputMode & MASK_SKB_LAYOUT);
        int charcase = (mInputMode & MASK_CASE);
        int variation = mEditorInfo.inputType & EditorInfo.TYPE_MASK_VARIATION;

        if (MASK_SKB_LAYOUT_PHONE != layout) {
            if (MASK_LANGUAGE_EN == language) {
                if (MASK_SKB_LAYOUT_QWERTY == layout) {
                    mToggleStates.mQwerty = true;
                    mToggleStates.mQwertyUpperCase = false;
                    states[statesNum] = mToggleStateEnLower;
                    if (MASK_CASE_UPPER == charcase) {
                        mToggleStates.mQwertyUpperCase = true;
                        states[statesNum] = mToggleStateEnUpper;
                    }
                    statesNum++;
                } else if (MASK_SKB_LAYOUT_SYMBOL1 == layout) {
                    states[statesNum] = mToggleStateEnSym1;
                    statesNum++;
                } else if (MASK_SKB_LAYOUT_SYMBOL2 == layout) {
                    states[statesNum] = mToggleStateEnSym2;
                    statesNum++;
                }
            } else if (MASK_SKB_LAYOUT_QWERTY_RU == layout) {
                mToggleStates.mQwerty = true;
                mToggleStates.mQwertyUpperCase = false;
                states[statesNum] = mToggleStateEnLower;
                if (MASK_CASE_UPPER == charcase) {
                    mToggleStates.mQwertyUpperCase = true;
                    states[statesNum] = mToggleStateEnUpper;
                }
                statesNum++;
            } else if (MASK_SKB_LAYOUT_QWERTY_UA == layout) {
                mToggleStates.mQwerty = true;
                mToggleStates.mQwertyUpperCase = false;
                states[statesNum] = mToggleStateEnLower;
                if (MASK_CASE_UPPER == charcase) {
                    mToggleStates.mQwertyUpperCase = true;
                    states[statesNum] = mToggleStateEnUpper;
                }
                statesNum++;
            }
            // Toggle rows for QWERTY.
            mToggleStates.mRowIdToEnable = KeyRow.DEFAULT_ROW_ID;
            if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) {
                mToggleStates.mRowIdToEnable = mToggleRowEmailAddress;
            } else if (variation == EditorInfo.TYPE_TEXT_VARIATION_URI) {
                mToggleStates.mRowIdToEnable = mToggleRowUri;
            } else if (MASK_LANGUAGE_EN == language || MASK_LANGUAGE_RU == language || MASK_LANGUAGE_UA == language) {
                mToggleStates.mRowIdToEnable = mToggleRowEn;
            }
        } else {
            if (MASK_CASE_UPPER == charcase) {
                states[statesNum] = mToggleStatePhoneSym;
                statesNum++;
            }
        }

        // Toggle state for enter key.
        int action = mEditorInfo.imeOptions
                & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION);

        if (action == EditorInfo.IME_ACTION_GO) {
            states[statesNum] = mToggleStateGo;
            statesNum++;
            mEnterKeyNormal = false;
        } else if (action == EditorInfo.IME_ACTION_SEARCH) {
            states[statesNum] = mToggleStateSearch;
            statesNum++;
            mEnterKeyNormal = false;
        } else if (action == EditorInfo.IME_ACTION_SEND) {
            states[statesNum] = mToggleStateSend;
            statesNum++;
            mEnterKeyNormal = false;
        } else if (action == EditorInfo.IME_ACTION_NEXT) {
            int f = mEditorInfo.inputType & EditorInfo.TYPE_MASK_FLAGS;
            if (f != EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE) {
                states[statesNum] = mToggleStateNext;
                statesNum++;
                mEnterKeyNormal = false;
            }
        } else if (action == EditorInfo.IME_ACTION_DONE) {
            states[statesNum] = mToggleStateDone;
            statesNum++;
            mEnterKeyNormal = false;
        }
        mToggleStates.mKeyStatesNum = statesNum;
    }

    CountryLanguage currentCountry = CountryLanguage.UKRAINIAN;

    private enum CountryLanguage {
        UKRAINIAN("ua", MODE_SKB_ENGLISH_LOWER, MODE_SKB_UKRAINIAN_LOWER, MODE_SKB_RUSSIAN_LOWER),
        RUSSIAN("ru", MODE_SKB_ENGLISH_LOWER, MODE_SKB_RUSSIAN_LOWER);

        String code;
        List<Integer> languages;

        CountryLanguage(String langCode, Integer... languages) {
            code = langCode;
            this.languages = Arrays.asList(languages);
        }

        int getNextLanguage(int current) {
            int newPosition = languages.indexOf(current) + 1;
            if (newPosition < 0 || newPosition >= languages.size()) {
                newPosition = 0;
            }
            return languages.get(newPosition);
        }

        static CountryLanguage getCountryByCode(String code) {
            for (CountryLanguage language : values()) {
                if (language.code.equalsIgnoreCase(code)) {
                    return language;
                }
            }
            return UKRAINIAN;
        }

    }
}
