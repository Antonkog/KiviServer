package com.wezom.kiviremoteserver.ui.views;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.wezom.kiviremoteserver.service.AspectLayoutService;

import java.util.List;

public class KeyListener implements View.OnKeyListener {
    List<View> list;
    View header;

    public KeyListener(@NonNull List<View> list, @NonNull View header) {
        this.header = header;
        this.list = list;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        int i = list.indexOf(v);

        if (keyCode == KeyEvent.KEYCODE_CHANNEL_UP)
            keyCode = KeyEvent.KEYCODE_DPAD_UP;
        if (keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN)
            keyCode = KeyEvent.KEYCODE_DPAD_DOWN;
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
            case KeyEvent.KEYCODE_BACK:
                if (event.getAction() == KeyEvent.ACTION_UP)
                    v.getContext().stopService(new Intent(v.getContext(), AspectLayoutService.class));
                return true;

            case KeyEvent.KEYCODE_DPAD_UP:
                if (i < 0) {
                    header.requestFocus();
                    return true;
                }
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    i++;
                    if (i < list.size()) {
                        list.get(i).requestFocus();
                    }

                }
                return true;


            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (i < 0) {
                    header.requestFocus();
                    return true;
                }
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    i--;
                    if (i >= 0) {
                        list.get(i).requestFocus();
                    } else {
                        header.requestFocus();
                    }
                }
                return true;


        }

        return false;
    }
}
