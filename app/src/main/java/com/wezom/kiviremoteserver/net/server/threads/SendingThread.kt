package com.wezom.kiviremoteserver.net.server.threads

import timber.log.Timber
import java.io.OutputStream
import java.io.PrintWriter
import java.net.Socket

class SendingThread(private val threadedModel: WriteThreadedModel<String>) : Thread() {
    private val clientSocket: Socket = threadedModel.socket

    private var isStopped = false

    override fun run() {
        var outputStream: OutputStream? = null
        var printWriter: PrintWriter? = null
        try {
            outputStream = clientSocket.getOutputStream();
           PrintWriter(outputStream, true).let { writer ->
                while (!isStopped) {
                    val message = threadedModel.queue.take()
                    if (message.length > 150)
                        Timber.d("Sending message: " + message.subSequence(0, 150))
                    else
                        Timber.d("Sending message: $message")
                    writer.println(message + "\r\n")
                    if (writer.checkError()) {
                        writer.close()
                        Timber.d("Error during writing")
                        interrupt()
                    }
                }
            }
        } catch (e: InterruptedException){
            isStopped = true
            Timber.e("SendingThread got InterruptedException error: " + e)
        } catch (e: Exception) {
            isStopped = true
            Timber.e("SendingThread got error: "+ e)
        } finally {
            isStopped = true
            outputStream?.close()
            printWriter?.close()
        }
    }

    fun stopSelf() {
        isStopped = true
        interrupt()
    }
}
//
//
///**
// * Created by andre on 05.06.2017.
// */
//
//public class SendingThread extends Thread {
//
//    private WriteThreadedModel<String> threadedModel;
//    private Socket clientSocket;
//
//    private boolean isStopped = false;
//
//    public SendingThread(WriteThreadedModel<String> threadedModel) {
//        this.threadedModel = threadedModel;
//        clientSocket = threadedModel.getSocket();
//    }
//
//    @Override
//    public void run() {
//        try (PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
//            while (!isStopped) {
//                String message = threadedModel.getQueue().take();
//                if (message.length() > 150)
//                    Timber.d("Sending message: " + message.subSequence(0, 150));
//                else
//                    Timber.d("Sending message: " + message);
//                writer.println(message + "\r\n");
//                if (writer.checkError()) {
//                    Timber.d("Error during writing");
//                    interrupt();
//                }
//            }
//        } catch (Exception e) {
//            Timber.e(e, e.getMessage());
//            if(Fabric.isInitialized()) {
//                Crashlytics.logException(e);
//            }
//        }
//        }
//
//    public void stopSelf() {
//        isStopped = true;
//    }
//}
