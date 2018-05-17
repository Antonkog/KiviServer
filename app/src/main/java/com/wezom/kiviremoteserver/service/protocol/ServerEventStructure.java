package com.wezom.kiviremoteserver.service.protocol;

import com.google.gson.annotations.SerializedName;
import com.wezom.kiviremoteserver.common.KiviProtocolStructure;
import com.wezom.kiviremoteserver.net.server.model.ServerApplicationInfo;

import java.util.List;

/**
 * Created by andre on 02.06.2017.
 */

public class ServerEventStructure {
    @SerializedName("event")
    private KiviProtocolStructure.ServerEventType event;
    @SerializedName("app_info")
    private List<ServerApplicationInfo> appInfo;
    @SerializedName("volume")
    private Integer volume;

    public ServerEventStructure(KiviProtocolStructure.ServerEventType event) {
        this.event = event;
    }

    public ServerEventStructure(List<ServerApplicationInfo> appInfo) {
        this.appInfo = appInfo;
    }

    public ServerEventStructure(KiviProtocolStructure.ServerEventType event, int volume) {
        this.event = event;
        this.volume = volume;
    }
}
