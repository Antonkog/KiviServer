package com.wezom.kiviremoteserver.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.wezom.kiviremoteserver.R;


public class MessageDialog extends DialogFragment implements DialogInterface.OnClickListener {
    private String message;
    private String title;

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme)
                .setTitle(title)
                .setPositiveButton(R.string.ok, this)
                .setMessage(message);
        return builder.create();
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                dismiss();
                break;
        }
    }
}
