package com.whitecloud.socket.core.interfaces

import java.lang.Exception

interface IIOManager {

    fun startEngine()

    fun send(sendable: ISendable)

    fun close()

    fun close(error: Exception)
}