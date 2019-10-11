package com.wezom.kiviremoteserver.net.server.threads

import com.crashlytics.android.Crashlytics
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
            printWriter = PrintWriter(outputStream, true)
            printWriter.use { writer ->
                while (!isStopped) {
                    val message = threadedModel.queue.take()
                    if (message.length > 150)
                        Timber.d("Sending message: " + message.subSequence(0, 150))
                    else
                        Timber.d("Sending message: $message")
                    writer.println(message + "\r\n")
                    if (writer.checkError()) {
                        Timber.d("Error during writing")
                        writer.close()
                        return
                    }
                }
            }
        } catch (e: Exception) {
            isStopped = true
            Timber.e(e, e.message)
            Crashlytics.logException(e)
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
