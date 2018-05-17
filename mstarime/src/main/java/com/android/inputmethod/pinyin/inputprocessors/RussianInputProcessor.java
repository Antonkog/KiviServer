package com.android.inputmethod.pinyin.inputprocessors;


import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

import com.android.inputmethod.pinyin.keycode.RussianKeyCodes;
import com.android.inputmethod.pinyin.keymap.CyrillicKeyMap;

/**
 * Class to handle Russian input.
 */
public class RussianInputProcessor {
    public boolean processKey(InputConnection inputContext, KeyEvent event,
                              boolean upperCase, boolean realAction) {
        if (null == inputContext || null == event) return false;

        int keyCode = event.getKeyCode();
//        int keyChar;
//        keyChar = 0;
//        if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
//            keyChar = keyCode - KeyEvent.KEYCODE_A + 'a';
//            if (upperCase) {
//                keyChar = keyChar + 'A' - 'a';
//            }
//        } else if (keyCode == KeyEvent.KEYCODE_COMMA)
//            keyChar = ',';
//        else if (keyCode == KeyEvent.KEYCODE_PERIOD)
//            keyChar = '.';
//        else if (keyCode == KeyEvent.KEYCODE_APOSTROPHE)
//            keyChar = '\'';
//        else if (keyCode == KeyEvent.KEYCODE_AT)
//            keyChar = '@';
//        else if ((keyCode == KeyEvent.KEYCODE_SLASH) && event.isShiftPressed()) {
//            keyChar = '?';
//        } else if ((keyCode == KeyEvent.KEYCODE_1) && event.isShiftPressed()) {
//            keyChar = '!';
//        } else if ((keyCode == KeyEvent.KEYCODE_3) && event.isShiftPressed()) {
//            keyChar = '#';
//        } else if ((keyCode == KeyEvent.KEYCODE_4) && event.isShiftPressed()) {
//            keyChar = '$';
//        } else if ((keyCode == KeyEvent.KEYCODE_5) && event.isShiftPressed()) {
//            keyChar = '%';
//        } else if ((keyCode == KeyEvent.KEYCODE_7) && event.isShiftPressed()) {
//            keyChar = '&';
//        } else if ((keyCode == KeyEvent.KEYCODE_8) && event.isShiftPressed()) {
//            keyChar = '*';
//        } else if ((keyCode == KeyEvent.KEYCODE_9) && event.isShiftPressed()) {
//            keyChar = '(';
//        } else if ((keyCode == KeyEvent.KEYCODE_0) && event.isShiftPressed()) {
//            keyChar = ')';
//        } else if (keyCode == KeyEvent.KEYCODE_SLASH)
//            keyChar = '/';
//        else if ((keyCode >= KeyEvent.KEYCODE_0
//                && keyCode <= KeyEvent.KEYCODE_9) && !event.isShiftPressed())
//            keyChar = keyCode - KeyEvent.KEYCODE_0 + '0';

        Integer ruKey;
        if (keyCode >= RussianKeyCodes.RUSSIAN_F && keyCode <= RussianKeyCodes.RUSSIAN_Z) {
            ruKey = keyCode;
        } else {
            ruKey = CyrillicKeyMap.getRussianKeys().get(keyCode);
        }

        if (ruKey == null) {
            String insert = null;
            if (KeyEvent.KEYCODE_DEL == keyCode) {
                if (realAction) {
                    inputContext.deleteSurroundingText(1, 0);
                }
            } else if (KeyEvent.KEYCODE_ENTER == keyCode) {
                insert = "\n";
            } else if (KeyEvent.KEYCODE_SPACE == keyCode) {
                insert = " ";
            } else {
                return false;
            }

            if (null != insert && realAction)
                inputContext.commitText(insert, insert.length());

            return true;
        }

        if (!realAction)
            return true;

        String result;
        if (upperCase) {
                result = String.valueOf((char) ruKey.intValue()).toUpperCase();
        } else {
                result = String.valueOf((char) ruKey.intValue());
        }

        inputContext.commitText(result, result.length());
        return true;
    }
}
