package com.android.inputmethod.pinyin.keymap;


import java.util.HashMap;

import static android.view.KeyEvent.KEYCODE_A;
import static android.view.KeyEvent.KEYCODE_B;
import static android.view.KeyEvent.KEYCODE_C;
import static android.view.KeyEvent.KEYCODE_COMMA;
import static android.view.KeyEvent.KEYCODE_D;
import static android.view.KeyEvent.KEYCODE_E;
import static android.view.KeyEvent.KEYCODE_F;
import static android.view.KeyEvent.KEYCODE_G;
import static android.view.KeyEvent.KEYCODE_H;
import static android.view.KeyEvent.KEYCODE_I;
import static android.view.KeyEvent.KEYCODE_J;
import static android.view.KeyEvent.KEYCODE_K;
import static android.view.KeyEvent.KEYCODE_L;
import static android.view.KeyEvent.KEYCODE_M;
import static android.view.KeyEvent.KEYCODE_N;
import static android.view.KeyEvent.KEYCODE_O;
import static android.view.KeyEvent.KEYCODE_P;
import static android.view.KeyEvent.KEYCODE_PERIOD;
import static android.view.KeyEvent.KEYCODE_Q;
import static android.view.KeyEvent.KEYCODE_R;
import static android.view.KeyEvent.KEYCODE_S;
import static android.view.KeyEvent.KEYCODE_T;
import static android.view.KeyEvent.KEYCODE_U;
import static android.view.KeyEvent.KEYCODE_V;
import static android.view.KeyEvent.KEYCODE_W;
import static android.view.KeyEvent.KEYCODE_X;
import static android.view.KeyEvent.KEYCODE_Y;
import static android.view.KeyEvent.KEYCODE_Z;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_A;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_B;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_C;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_COMMA;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_D;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_E;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_F;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_G;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_H;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_I;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_J;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_K;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_L;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_M;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_N;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_O;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_P;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_PERIOD;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_Q;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_R;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_S;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_T;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_U;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_V;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_W;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_X;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_Y;
import static com.android.inputmethod.pinyin.keycode.RussianKeyCodes.RUSSIAN_Z;
import static com.android.inputmethod.pinyin.keycode.UkrainianKeyCodes.UKRAINIAN_S;

public class CyrillicKeyMap {

    public static HashMap<Integer, Integer> keys;

    static {
        initKeys();
    }

    private static void initKeys() {
        keys = new HashMap<>();
        keys.put(KEYCODE_Q, RUSSIAN_Q);
        keys.put(KEYCODE_W, RUSSIAN_W);
        keys.put(KEYCODE_E, RUSSIAN_E);
        keys.put(KEYCODE_R, RUSSIAN_R);
        keys.put(KEYCODE_T, RUSSIAN_T);
        keys.put(KEYCODE_Y, RUSSIAN_Y);
        keys.put(KEYCODE_U, RUSSIAN_U);
        keys.put(KEYCODE_I, RUSSIAN_I);
        keys.put(KEYCODE_O, RUSSIAN_O);
        keys.put(KEYCODE_P, RUSSIAN_P);
        keys.put(KEYCODE_A, RUSSIAN_A);
        keys.put(KEYCODE_S, RUSSIAN_S);
        keys.put(KEYCODE_D, RUSSIAN_D);
        keys.put(KEYCODE_F, RUSSIAN_F);
        keys.put(KEYCODE_G, RUSSIAN_G);
        keys.put(KEYCODE_H, RUSSIAN_H);
        keys.put(KEYCODE_J, RUSSIAN_J);
        keys.put(KEYCODE_K, RUSSIAN_K);
        keys.put(KEYCODE_L, RUSSIAN_L);

        keys.put(KEYCODE_Z, RUSSIAN_Z);
        keys.put(KEYCODE_X, RUSSIAN_X);
        keys.put(KEYCODE_C, RUSSIAN_C);
        keys.put(KEYCODE_V, RUSSIAN_V);
        keys.put(KEYCODE_B, RUSSIAN_B);
        keys.put(KEYCODE_N, RUSSIAN_N);
        keys.put(KEYCODE_M, RUSSIAN_M);
        keys.put(KEYCODE_PERIOD, RUSSIAN_PERIOD);
        keys.put(KEYCODE_COMMA, RUSSIAN_COMMA);
    }

    public static HashMap<Integer, Integer> getUkrainanKeys() {
        if (keys == null)
            initKeys();
        keys.put(KEYCODE_S, UKRAINIAN_S);
        return keys;
    }

    public static HashMap<Integer, Integer> getRussianKeys() {
        if (keys == null)
            initKeys();
        keys.put(KEYCODE_S, RUSSIAN_S);
        return keys;
    }
}
