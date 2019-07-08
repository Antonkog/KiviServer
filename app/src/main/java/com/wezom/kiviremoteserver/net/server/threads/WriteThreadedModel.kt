package com.wezom.kiviremoteserver.net.server.threads

import java.net.Socket
import java.util.concurrent.BlockingQueue

class WriteThreadedModel<S>(val queue: BlockingQueue<S>, val socket: Socket)
