package com.wezom.kiviremoteserver.service.protocol;

import android.os.Build;

import com.google.gson.annotations.SerializedName;
import com.wezom.kiviremoteserver.common.KiviProtocolStructure;
import com.wezom.kiviremoteserver.interfaces.AspectAvailable;
import com.wezom.kiviremoteserver.interfaces.AspectMessage;
import com.wezom.kiviremoteserver.interfaces.InitialMessage;
import com.wezom.kiviremoteserver.net.server.model.Channel;
import com.wezom.kiviremoteserver.net.server.model.Input;
import com.wezom.kiviremoteserver.net.server.model.Recommendation;
import com.wezom.kiviremoteserver.net.server.model.ServerApplicationInfo;

import java.util.List;

/**
 * Created by andre on 02.06.2017.
 */

public class ServerEventStructure {
    private KiviProtocolStructure.ServerEventType event;
    @SerializedName("app_info")
    private List<ServerApplicationInfo> appInfo;
    private List<Recommendation> recommendations;
    private List<Recommendation> favorites;
    private List<Channel> channels;
    private List<Input> inputs;
    private Integer volume;
    private AspectMessage aspectMessage;
    private AspectAvailable availableAspectValues;
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


    public ServerEventStructure setAvailableInputs(List<Input> inputs) {
        this.inputs = inputs;
        return this;
    }


    public ServerEventStructure setAvailableRecommendations(List<Recommendation> recommendations) {
        this.recommendations = recommendations;
        return this;
    }

    public ServerEventStructure setAvailableChannels(List<Channel> channels) {
        this.channels = channels;
        return this;
    }

    public ServerEventStructure setAvailableFavourites(List<Recommendation> favorites) {
        this.favorites = favorites;
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
            appInfo.stream().forEach( app ->  sb.append(app.toString()));
        }else {
            sb.append("apps size= " + appInfo.size() );
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
