package com.wezom.kiviremoteserver.keyboardsample.softkeyboard;


import java.lang.ref.SoftReference;
import java.util.HashMap;

public enum KeyboardSwitcher {
    INSTANCE;

    private LatinIME inputMethodService;
    private final HashMap<String, SoftReference<LatinKeyboard>> mKeyboards = new HashMap<>();


    KeyboardSwitcher() {
    }

    public void init(LatinIME ims) {
        inputMethodService = ims;

    }

    public void makeKeyboards(boolean forceCreate) {

        if (forceCreate)
            mKeyboards.clear();
        // Configuration change is coming after the keyboard gets recreated. So
        // don't rely on that.
        // If keyboards have already been made, check if we have a screen width
        // change and
        // create the keyboard layouts again at the correct orientation
        if (!forceCreate)
            mKeyboards.clear();
    }
}
