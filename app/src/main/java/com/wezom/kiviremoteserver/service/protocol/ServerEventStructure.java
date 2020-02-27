package com.wezom.kiviremoteserver.service.protocol;

import android.os.Build;

import com.google.gson.annotations.SerializedName;
import com.wezom.kiviremoteserver.common.KiviProtocolStructure;
import com.wezom.kiviremoteserver.interfaces.AspectAvailable;
import com.wezom.kiviremoteserver.interfaces.AspectMessage;
import com.wezom.kiviremoteserver.interfaces.InitialMessage;
import com.wezom.kiviremoteserver.net.server.model.PreviewCommonStructure;
import com.wezom.kiviremoteserver.net.server.model.PreviewContent;
import com.wezom.kiviremoteserver.net.server.model.ServerApplicationInfo;

import java.util.List;

/**
 * Created by andre on 02.06.2017.
 */

public class ServerEventStructure {
    private KiviProtocolStructure.ServerEventType event;
    @SerializedName("app_info")
    private List<ServerApplicationInfo> appInfo;
//
//    @SerializedName("args")
//    @Expose
//    private List<String> args;

    private Integer volume;
    private AspectMessage aspectMessage;
    private AspectAvailable availableAspectValues;
    private List<PreviewCommonStructure> previewCommonStructures;
    private List<PreviewContent> previewContents;
    private InitialMessage initialMessage;

    public ServerEventStructure(AspectMessage aspectMessage, AspectAvailable aspectAvailable, InitialMessage initialMsg, KiviProtocolStructure.ServerEventType event) {
        this.aspectMessage = aspectMessage;
        this.availableAspectValues = aspectAvailable;
        this.initialMessage = initialMsg;
        this.event = event;
    }

    public ServerEventStructure(KiviProtocolStructure.ServerEventType event) {
        this.event = event;
    }
//
//    public ServerEventStructure addStringArgs(List<String> args) {
//        this.args = args;
//        return this;
//    }

    public ServerEventStructure addPreviewContents(List<PreviewContent> previewContents) {
        this.previewContents = previewContents;
        return this;
    }

    public ServerEventStructure addPreviewCommonStructures(List<PreviewCommonStructure> previewCommonStructures) {
        this.previewCommonStructures = previewCommonStructures;
        return this;
    }


    public ServerEventStructure addApps(List<ServerApplicationInfo> appInfo) {
        this.appInfo = appInfo;
        return this;
    }

    public ServerEventStructure(KiviProtocolStructure.ServerEventType event, int volume) {
        this.event = event;
        this.volume = volume;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            appInfo.stream().forEach(app -> sb.append(app.toString()));
        } else {
            sb.append("apps size= " + appInfo.size());
        }
        return "ServerEventStructure{" +
                "event=" + event +
                ", appInfo=" + sb.toString() +
                ", volume=" + volume +
                ", aspectMessage=" + aspectMessage +
                ", availableAspectValues=" + availableAspectValues +
                ", initialMessage=" + initialMessage +
                " apps size = " + appInfo.size() +
                '}';
    }
}
