package com.wezom.kiviremoteserver.net.server.model;

import com.wezom.kiviremoteserver.interfaces.ThreadServerHandler;

import java.net.Socket;

/**
 * Created by andre on 05.06.2017.
 */
public class ReadThreadedModel<S> {
    ThreadServerHandler<S> handler;
    Socket socket;

    public ReadThreadedModel(ThreadServerHandler<S> handler, Socket socket) {
        this.handler = handler;
        this.socket = socket;
    }

    public ThreadServerHandler<S> getHandler() {
        return handler;
    }

    public Socket getSocket() {
        return socket;
    }
}
