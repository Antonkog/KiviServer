package com.wezom.kiviremoteserver.net.server.threads;

import com.wezom.kiviremoteserver.net.server.model.WriteThreadedModel;

import java.io.PrintWriter;
import java.net.Socket;

import timber.log.Timber;

/**
 * Created by andre on 05.06.2017.
 */

public class SendingThread extends Thread {

    private WriteThreadedModel<String> threadedModel;
    private Socket clientSocket;

    private boolean isStopped = false;

    public SendingThread(WriteThreadedModel<String> threadedModel) {
        this.threadedModel = threadedModel;
        clientSocket = threadedModel.getSocket();
    }

    @Override
    public void run() {
        try (PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
            while (!isStopped) {
                String message = threadedModel.getQueue().take();
                if (message.length() > 150)
                    Timber.d("Sending message: " + message.subSequence(0, 150));
                else
                    Timber.d("Sending message: " + message);
                writer.println(message + "\r\n");
                if (writer.checkError()) {
                    Timber.d("Error during writing");
                    interrupt();
                }
            }
        } catch (Exception e) {
            Timber.e(e, e.getMessage());
        }
    }

    public void stopSelf() {
        isStopped = true;
    }
}
