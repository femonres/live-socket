package com.whitecloud.socket.core.interfaces

import java.io.InputStream

interface ISocketReader {

    fun initialize(inputStream: InputStream, stateSender: IStateSender)

    @Throws(RuntimeException::class)
    fun read()

    fun close()
}