package com.whitecloud.socket.core.interfaces

import java.io.OutputStream

interface ISocketWriter {

    fun initialize(outputStream: OutputStream, stateSender: IStateSender)

    @Throws(RuntimeException::class)
    fun write()

    fun offer(sendable: ISendable)

    fun close()
}