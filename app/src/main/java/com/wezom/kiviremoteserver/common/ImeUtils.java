package com.wezom.kiviremoteserver.common;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;

import com.wezom.kiviremoteserver.BuildConfig;
import com.wezom.kiviremoteserver.service.ExecutorServiceIME;

import timber.log.Timber;

/**
 * Created by andre on 05.06.2017.
 */

public class ImeUtils {
    private static InputMethodManager imeManager;

    public static void showInputMethodPicker(Context context) {
        if (imeManager == null)
            imeManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imeManager != null) {
            imeManager.showInputMethodPicker();
        } else
            Timber.e("InputMethodManager is null");
    }

    public static String getCurrentImeKeyboardInfo(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
    }

    public static boolean isCurrentImeOk(Context context) {
        String targetImeId = BuildConfig.APPLICATION_ID + "/.service." + ExecutorServiceIME.class.getSimpleName();
        return TextUtils.equals(targetImeId, ImeUtils.getCurrentImeKeyboardInfo(context));
    }

}
