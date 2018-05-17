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

package com.android.inputmethod.pinyin.inputprocessors;

import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

/**
 * Class to handle English input.
 */
public class EnglishInputProcessor {
    public boolean processKey(InputConnection inputContext, KeyEvent event,
                              boolean upperCase, boolean realAction) {
        if (null == inputContext || null == event) return false;

        int keyCode = event.getKeyCode();
        int keyChar;
        keyChar = 0;

        if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
            keyChar = keyCode - KeyEvent.KEYCODE_A + 'a';
            if (upperCase) {
                keyChar = keyChar + 'A' - 'a';
            }
        }
//        else if (keyCode == KeyEvent.KEYCODE_COMMA)
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
//        else if (keyCode >= KeyEvent.KEYCODE_0
//                && keyCode <= KeyEvent.KEYCODE_9)
//            keyChar = keyCode - KeyEvent.KEYCODE_0 + '0';

        if (0 == keyChar) {
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

        String result = String.valueOf((char) keyChar);
        inputContext.commitText(result, result.length());
        return true;
    }
}
