package com.wezom.kiviremoteserver.service.protocol;

import com.google.gson.annotations.SerializedName;
import com.wezom.kiviremoteserver.common.KiviProtocolStructure;
import com.wezom.kiviremoteserver.interfaces.AspectAvailable;
import com.wezom.kiviremoteserver.interfaces.AspectMessage;
import com.wezom.kiviremoteserver.net.server.model.ServerApplicationInfo;

import java.util.List;

/**
 * Created by andre on 02.06.2017.
 */

public class ServerEventStructure {
    private KiviProtocolStructure.ServerEventType event;
    @SerializedName("app_info")
    private List<ServerApplicationInfo> appInfo;
    private Integer volume;
    private AspectMessage aspectMessage;
    private AspectAvailable availableAspectValues;

    public ServerEventStructure(AspectMessage aspectMessage, AspectAvailable aspectAvailable) {
        this.aspectMessage = aspectMessage;
        this.availableAspectValues = aspectAvailable;
    }

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
