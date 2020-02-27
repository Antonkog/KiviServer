package com.wezom.kiviremoteserver.mvp.view;

import com.wezom.kiviremoteserver.service.protocol.ServerEventStructure;

/**
 * Created by andre on 05.06.2017.
 */

public interface ServiceMvpView {
    void registerNsd(int port);
    void unregisterNsd();
    void sendBySocket(ServerEventStructure structure);
}
