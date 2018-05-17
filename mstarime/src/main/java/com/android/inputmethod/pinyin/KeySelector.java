package com.android.inputmethod.pinyin;


import android.util.Pair;

public enum KeySelector {
    INSTANCE;

    private Pair<Integer, Integer> keysToSelect;

    public void setKeysToSelect(Pair<Integer, Integer> keys) {
        keysToSelect = keys;
    }

    public Pair<Integer, Integer> getKeysToSelect() {
        return keysToSelect;
    }
}
