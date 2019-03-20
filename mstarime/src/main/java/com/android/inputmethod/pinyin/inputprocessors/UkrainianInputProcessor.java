package com.android.inputmethod.pinyin.inputprocessors;


import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

import com.android.inputmethod.pinyin.keycode.RussianKeyCodes;
import com.android.inputmethod.pinyin.keycode.UkrainianKeyCodes;
import com.android.inputmethod.pinyin.keymap.CyrillicKeyMap;

/**
 * Class to handle Russian input.
 */
public class UkrainianInputProcessor implements ProcessorStrategy {
    public boolean processKey(InputConnection inputContext, KeyEvent event,
                              boolean upperCase, boolean realAction) {
        if (null == inputContext || null == event) return false;

        int keyCode = event.getKeyCode();
        Integer ruKey;
        if (keyCode >= RussianKeyCodes.RUSSIAN_F && keyCode <= UkrainianKeyCodes.UKRAINIAN_G) {
            ruKey = keyCode;
        } else {
            ruKey = CyrillicKeyMap.getUkrainanKeys().get(keyCode);
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