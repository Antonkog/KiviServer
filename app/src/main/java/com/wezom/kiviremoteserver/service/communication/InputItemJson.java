package com.wezom.kiviremoteserver.service.communication;

import android.content.Context;

import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper;

import java.io.Serializable;

public class InputItemJson implements Serializable {
    int id;
    String name;
    boolean isConnected;

    protected InputItemJson(InputSourceHelper.INPUT_PORT port, Context context) {
        id = port.getId();
        name = context.getString(port.getNameResource());
        isConnected = port.isConnected();
    }
}
