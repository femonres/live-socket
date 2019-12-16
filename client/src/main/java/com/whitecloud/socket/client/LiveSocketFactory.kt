package com.whitecloud.socket.client

import java.net.Socket

abstract class LiveSocketFactory {

    @Throws(Exception::class)
    abstract fun createSocket(info: ConnectionInfo, options: LiveSocketOptions): Socket
}
