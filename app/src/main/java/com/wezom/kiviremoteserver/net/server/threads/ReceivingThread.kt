package com.wezom.kiviremoteserver.net.server.threads

import com.wezom.kiviremoteserver.bus.NewMessageEvent
import com.wezom.kiviremoteserver.common.RxBus
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.Socket


class ReceivingThread(private val socket: Socket) : Thread() {

    private var isRunning = true

    override fun run() {
        Timber.d("Start receiving")
        val stream: InputStream? = null
        try {
            val stream: InputStream = socket.getInputStream()
            val reader = BufferedReader(InputStreamReader(stream))
            var message: String? = ""

            while (isRunning && (reader.readLine().also { message = it } != null)) {
                message?.takeIf { it.isNotEmpty() }?.let { RxBus.publish(NewMessageEvent(it)) }
            }
        } catch (e: Exception) {
            isRunning = false
            Timber.e(e, e.message)
        } finally {
            isRunning = false
            stream?.close()
        }
    }

    fun stopSelf() {
        isRunning = false
        interrupt()
    }
}