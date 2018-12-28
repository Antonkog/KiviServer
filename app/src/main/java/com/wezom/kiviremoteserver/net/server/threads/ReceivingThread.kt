package com.wezom.kiviremoteserver.net.server.threads

import com.crashlytics.android.Crashlytics
import com.wezom.kiviremoteserver.bus.NewMessageEvent
import com.wezom.kiviremoteserver.common.RxBus
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket


class ReceivingThread(private val socket: Socket) : Thread() {

    private var isRunning = true

    override fun run() {
        Timber.d("Start receiving")
        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        var message: String? = ""
        try {
            while (isRunning && (reader.readLine().also { message = it } != null)) {
                message?.takeIf { it.isNotEmpty() }?.let { RxBus.publish(NewMessageEvent(it)) }
            }
        } catch (e: IOException) {
            if(Fabric.isInitialized()) {
                Crashlytics.logException(e);
            }
            Timber.e(e, e.message)
        }
    }

    fun stopSelf() {
        isRunning = false
    }
}