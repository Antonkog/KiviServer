package com.android.inputmethod.pinyin.inputprocessors;

import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

import com.android.inputmethod.pinyin.ComposingView;
import com.android.inputmethod.pinyin.InputModeSwitcher;

public class InputProcessor {

    private EnglishInputProcessor enProc;
    private RussianInputProcessor ruProc;
    private UkrainianInputProcessor uaProc;
    private DefaultInputProcessor defaultInProc;

    public InputProcessor() {
        enProc = new EnglishInputProcessor();
        ruProc = new RussianInputProcessor();
        uaProc = new UkrainianInputProcessor();
        defaultInProc = new DefaultInputProcessor();
    }

    private ProcessorStrategy getProcessorStrategy(int mode) {
        switch (mode) {
            case InputModeSwitcher.MODE_SKB_ENGLISH_LOWER:
            case InputModeSwitcher.MODE_SKB_ENGLISH_UPPER:
                return enProc;
            case InputModeSwitcher.MODE_SKB_RUSSIAN_LOWER:
            case InputModeSwitcher.MODE_SKB_RUSSIAN_UPPER:
                return ruProc;
            case InputModeSwitcher.MODE_SKB_UKRAINIAN_LOWER:
            case InputModeSwitcher.MODE_SKB_UKRAINIAN_UPPER:
                return uaProc;
            default:
                return defaultInProc;
        }
    }

    public boolean processKey(int mode, InputConnection inputContext, KeyEvent event,
                              boolean upperCase, boolean realAction, ComposingView view) {
        boolean processing = getProcessorStrategy(mode).processKey(inputContext, event, upperCase, realAction);
        if (getProcessorStrategy(mode) instanceof DefaultInputProcessor) {
            view.invalidate();
        }
        return processing;
    }
}
