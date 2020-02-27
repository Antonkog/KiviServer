package com.wezom.kiviremoteserver.interfaces;

import android.content.Context;
import android.util.Pair;

import com.wezom.kiviremoteserver.net.server.model.PreviewContent;
import com.wezom.kiviremoteserver.service.protocol.ServerEventStructure;

import java.net.ServerSocket;
import java.util.List;

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

    void sendAspect(AspectMessage aspectMessage, AspectAvailable available);

    void sendImgById(List<PreviewContent> previewContents);

    void sendInitialMsg(AspectMessage aspectMessage, AspectAvailable available, InitialMessage initialMessage);

    void disposeResources();

    Pair<String, String> getLocalIpPair(Context context);
}
