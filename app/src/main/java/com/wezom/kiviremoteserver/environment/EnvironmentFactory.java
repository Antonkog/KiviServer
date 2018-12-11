package com.wezom.kiviremoteserver.environment;

import android.content.Context;

public class EnvironmentFactory {
    public static final int ENVIRONMENT_MTC = 0;
    public static final int ENVIRONMENT_REALTEC = 1;
    private Context context;

    public EnvironmentFactory(Context context) {
        this.context = context;
//        BridgeGeneral bridgeGeneral = new BridgeGeneral();
    }


}
