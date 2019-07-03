package com.wezom.kiviremoteserver.bus;


import com.wezom.kiviremoteserver.net.server.model.ServerApplicationInfo;

import java.util.List;

public class SendAppsListEvent {
    private List<ServerApplicationInfo> serverApplicationInfos;

    public SendAppsListEvent(List<ServerApplicationInfo> serverApplicationInfos) {
        this.serverApplicationInfos = serverApplicationInfos;
    }

    public List<ServerApplicationInfo> getServerApplicationInfos() {
        return serverApplicationInfos;
    }
}
