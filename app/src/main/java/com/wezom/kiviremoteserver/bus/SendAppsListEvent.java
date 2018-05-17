package com.wezom.kiviremoteserver.bus;


import android.content.pm.ApplicationInfo;

import java.util.List;

public class SendAppsListEvent {
    private List<ApplicationInfo> appInfoList;

    public SendAppsListEvent(List<ApplicationInfo> appInfoList) {
        this.appInfoList = appInfoList;
    }

    public List<ApplicationInfo> getAppInfoList() {
        return appInfoList;
    }
}
