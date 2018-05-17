package com.wezom.kiviremoteserver.net.server.model;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * Created by andre on 05.06.2017.
 */
public class WriteThreadedModel<S> {
    private BlockingQueue<S> queue;
    private Socket socket;

    public WriteThreadedModel(BlockingQueue<S> queue, Socket socket) {
        this.queue = queue;
        this.socket = socket;
    }

    public BlockingQueue<S> getQueue() {
        return queue;
    }

    public Socket getSocket() {
        return socket;
    }
}
