package com.wezom.kiviremoteserver.net.server.threads;

import com.wezom.kiviremoteserver.net.server.model.ReadThreadedModel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import timber.log.Timber;

/**
 * Created by andre on 05.06.2017.
 */

public class LegacyReceivingThread extends Thread {
    private Socket clientSocket;
    private boolean isRunning = true;
    private ReadThreadedModel<String> threadedModel;

    public LegacyReceivingThread(ReadThreadedModel<String> threadedModel) {
        this.threadedModel = threadedModel;
        clientSocket = threadedModel.getSocket();
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String wasRead = reader.readLine();
            while (isRunning && clientSocket.isConnected() && wasRead != null) {
                wasRead = reader.readLine();
                if (wasRead != null && wasRead.startsWith("{"))
                    threadedModel.getHandler().newMessageRead(wasRead);
            }
        } catch (Exception e) {
            Timber.e(e, e.getMessage());
        }
    }

    public void stopSelf() {
        isRunning = false;
    }
}
