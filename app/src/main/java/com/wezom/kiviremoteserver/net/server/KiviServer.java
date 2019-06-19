package com.wezom.kiviremoteserver.net.server;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Pair;

import com.google.gson.Gson;
import com.wezom.kiviremoteserver.bus.SendToSettingsEvent;
import com.wezom.kiviremoteserver.bus.SocketAcceptedEvent;
import com.wezom.kiviremoteserver.common.ImeUtils;
import com.wezom.kiviremoteserver.common.KiviProtocolStructure;
import com.wezom.kiviremoteserver.common.RxBus;
import com.wezom.kiviremoteserver.common.Utils;
import com.wezom.kiviremoteserver.interfaces.AspectAvailable;
import com.wezom.kiviremoteserver.interfaces.AspectMessage;
import com.wezom.kiviremoteserver.interfaces.InitialMessage;
import com.wezom.kiviremoteserver.interfaces.RemoteServer;
import com.wezom.kiviremoteserver.mvp.view.ServiceMvpView;
import com.wezom.kiviremoteserver.net.server.model.Channel;
import com.wezom.kiviremoteserver.net.server.model.Input;
import com.wezom.kiviremoteserver.net.server.model.Recommendation;
import com.wezom.kiviremoteserver.net.server.model.WriteThreadedModel;
import com.wezom.kiviremoteserver.net.server.threads.ReceivingThread;
import com.wezom.kiviremoteserver.net.server.threads.SendingThread;
import com.wezom.kiviremoteserver.service.protocol.ServerEventStructure;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import timber.log.Timber;

/**
 * Created by andre on 02.06.2017.
 */

public class KiviServer implements RemoteServer {

    public static final String KEY_MESSAGE = "message";

    private static final int Q_CAPACITY = 100;

    private ServiceMvpView view;
    private ServerThread serverThread;
    private ServerSocket serverSocket;
    private BlockingQueue<String> queue;
    private Handler handler;
    private Context context;
    private Gson gson;

    private SendingThread sendingThread;
    private ReceivingThread receivingThread;

    public KiviServer(Handler handler, ServiceMvpView view, Context context) {
        this.handler = handler;
        this.view = view;
        this.context = context;
        gson = new Gson();
        serverThread = new ServerThread();
        queue = new ArrayBlockingQueue<>(Q_CAPACITY);
    }

    @Override
    public void launchServer() {
        serverThread.start();
    }

    private void dispose() {
        try {
            serverSocket.close();
        } catch (Exception e) {
            Timber.e(e, e.getMessage());
        }

        if (serverThread != null)
            serverThread.interrupt();
        if (sendingThread != null)
            sendingThread.interrupt();
        if (receivingThread != null)
            receivingThread.interrupt();

        view.unregisterNsd();
    }

    public void registerNSD(ServerSocket serverSocket) {
        view.registerNsd(serverSocket.getLocalPort());
    }

    public void postMessage(ServerEventStructure structure) {
        String structureJson = gson.toJson(structure, ServerEventStructure.class);
        if (structureJson.length() > 150)
            Timber.d("POST MESSAGE: " + structureJson.substring(0, 150));
        else
            Timber.d("POST MESSAGE: " + structureJson);
        queue.offer(structureJson);
    }

    public void dispatchRequest(String input) {
        sendMessage(input);

        if (!ImeUtils.isCurrentImeOk(context)) {
            postMessage(new ServerEventStructure(KiviProtocolStructure.ServerEventType.KEYBOARD_NOT_SET));
            RxBus.INSTANCE.publish(new SendToSettingsEvent());
        }
    }

    @Override
    public void stopReceiving() {
        if (receivingThread != null)
            receivingThread.stopSelf();
    }

    @Override
    public void disposeResources() {
        dispose();
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(0);
            registerNSD(serverSocket);

            while (!Thread.currentThread().isInterrupted()) {
                listenForConnections();
            }
        } catch (IOException e) {
            tearDown();
            Timber.e(e, e.getMessage());
        }
    }

    private void listenForConnections() {
        try {
            Socket clientSocket = serverSocket.accept();
            launchReadWriteThreads(clientSocket);
            RxBus.INSTANCE.publish(new SocketAcceptedEvent());
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
        }
    }

    private void launchReadWriteThreads(Socket clientSocket) {
        if (receivingThread != null)
            receivingThread.stopSelf();
        receivingThread = new ReceivingThread(clientSocket);
        receivingThread.start();

        if (sendingThread != null)
            sendingThread.stopSelf();
        sendingThread = new SendingThread(new WriteThreadedModel<>(queue, clientSocket));
        sendingThread.start();
    }

    @Override
    public Pair<String, String> getLocalIpPair(Context context) {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info;
        String ip = "";
        if (manager != null) {
            info = manager.getConnectionInfo();
            int ipAddress = info.getIpAddress();
            ip = String.format(Locale.ENGLISH, "%d.%d.%d.%d",
                    (ipAddress & 0xff),
                    (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff),
                    (ipAddress >> 24 & 0xff)
            );
        }
        int localPort = 0;
        if (serverSocket != null) {
            localPort = serverSocket.getLocalPort();
        }
        return new Pair<>(ip, String.valueOf(localPort));
    }

    private void tearDown() {
        //todo release resources
        view.unregisterNsd();
    }

    private void sendMessage(String mess) {
        Bundle messageBundle = new Bundle();
        messageBundle.putString(KEY_MESSAGE, mess);
        Message message = new Message();
        message.setData(messageBundle);
        handler.sendMessage(message);
    }

    @Override
    public void sendPong() {
        postMessage(new ServerEventStructure(KiviProtocolStructure.ServerEventType.PONG));
    }


    @Override
    public void sendAspect(AspectMessage aspectMessage, AspectAvailable available) {
        postMessage(new ServerEventStructure(aspectMessage, available,  null, KiviProtocolStructure.ServerEventType.ASPECT));
    }

    @Override
    public void sendInputs(List<Input> inputs) {
        if(!inputs.isEmpty())
        Utils.appendLog(" sending from server " + inputs.get(0).getName());
        postMessage(new ServerEventStructure(KiviProtocolStructure.ServerEventType.INPUTS).setAvailableInputs(inputs));
    }

    @Override
    public void sendRecommendations(List<Recommendation> recommendations) {
        if(!recommendations.isEmpty())
            Utils.appendLog(" sending from server " + recommendations.get(0).getName());
        postMessage(new ServerEventStructure(KiviProtocolStructure.ServerEventType.RECOMMENDATIONS).setAvailableRecommendations(recommendations));
    }

    @Override
    public void sendFavourites(List<Recommendation> favourites) {
        if(!favourites.isEmpty())
            Utils.appendLog(" sending from server " + favourites.get(0).getName());
        postMessage(new ServerEventStructure(KiviProtocolStructure.ServerEventType.FAVORITES).setAvailableFavourites(favourites));
    }

    @Override
    public void sendChannels(List<Channel> channels) {
        if(!channels.isEmpty())
            Utils.appendLog(" sending from server " + channels.get(0).getName());
        postMessage(new ServerEventStructure(KiviProtocolStructure.ServerEventType.CHANNELS).setAvailableChannels(channels));
    }

    @Override
    public void sendInitialMsg(AspectMessage aspectMessage, AspectAvailable available, InitialMessage msg) {
        postMessage(new ServerEventStructure(aspectMessage, available,  msg, KiviProtocolStructure.ServerEventType.INITIAL));
    }

    private class ServerThread extends Thread {
        @Override
        public void run() {
            try {
                startServer();
            } catch (Exception ie) {
                tearDown();
                Timber.e(ie, ie.getMessage());
            }
        }
    }
}
