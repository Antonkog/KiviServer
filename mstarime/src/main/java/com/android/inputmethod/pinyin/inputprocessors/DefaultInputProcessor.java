package com.android.inputmethod.pinyin.inputprocessors;


import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

/**
 * Class to handle Russian input.
 */
public class DefaultInputProcessor implements ProcessorStrategy {
    public boolean processKey(InputConnection inputContext, KeyEvent event,
                              boolean upperCase, boolean realAction) {
        if (null == inputContext || null == event) return false;

        int keyCode =  event.getKeyCode();

        int keyChar = 0;
        if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
            keyChar = keyCode - KeyEvent.KEYCODE_A + 'a';
        } else if (keyCode >= KeyEvent.KEYCODE_0
                && keyCode <= KeyEvent.KEYCODE_9) {
            keyChar = keyCode - KeyEvent.KEYCODE_0 + '0';
        } else if (keyCode == KeyEvent.KEYCODE_COMMA) {
            keyChar = ',';
        } else if (keyCode == KeyEvent.KEYCODE_PERIOD) {
            keyChar = '.';
        } else if (keyCode == KeyEvent.KEYCODE_SPACE) {
            keyChar = ' ';
        } else if (keyCode == KeyEvent.KEYCODE_APOSTROPHE) {
            keyChar = '\'';
        }
        if (0 != keyChar && realAction) {
           inputContext.commitText(String.valueOf((char) keyChar),1);
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) return false;
        return true;
    }
}