package com.whitecloud.socket.core.interfaces

import java.io.Serializable

interface ISendable : Serializable {

    fun parse(): ByteArray
}