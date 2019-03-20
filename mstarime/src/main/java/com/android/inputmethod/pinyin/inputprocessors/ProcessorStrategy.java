package com.android.inputmethod.pinyin.inputprocessors;

import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

public interface ProcessorStrategy {

    boolean processKey(InputConnection inputContext, KeyEvent event,
                       boolean upperCase, boolean realAction);
}
