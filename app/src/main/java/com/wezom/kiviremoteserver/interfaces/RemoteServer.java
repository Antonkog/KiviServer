package com.wezom.kiviremoteserver.interfaces;

import android.content.Context;
import android.util.Pair;

import com.wezom.kiviremoteserver.service.protocol.ServerEventStructure;

import java.net.ServerSocket;

/**
 * Created by andre on 02.06.2017.
 */

public interface RemoteServer {
    void launchServer();

    void registerNSD(ServerSocket serverSocket);

    void postMessage(ServerEventStructure structure);

    void dispatchRequest(String input);

    void stopReceiving();

    void sendPong();

    void sendAspect(AspectMessage aspectMessage, AspectAvailable availableValuesJson);

    void disposeResources();

    Pair<String, String> getLocalIpPair(Context context);
}
