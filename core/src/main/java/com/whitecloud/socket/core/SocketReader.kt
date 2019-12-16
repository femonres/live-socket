package com.whitecloud.socket.core

import com.whitecloud.socket.core.exceptions.ReadException
import com.whitecloud.socket.core.interfaces.ISocketReader
import com.whitecloud.socket.core.interfaces.IStateSender
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class SocketReader : ISocketReader {

    private lateinit var inputStream: InputStream
    private lateinit var stateSender: IStateSender

    override fun initialize(inputStream: InputStream, stateSender: IStateSender) {
        this.inputStream = inputStream
        this.stateSender = stateSender
    }

    override fun read() {
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val readLine = bufferedReader.readLine()
        if (readLine != null) {
            //println("SocketReader.read -> Lo que se lee es: $readLine")
        } else {
            //println("SocketReader.read -> read body is wrong, that mean this socket is disconnected by server")
            throw ReadException("read body is wrong, that mean this socket is disconnected by server")
        }

        stateSender.sendBroadcast(IStateSender.State.ACTION_READ_COMPLETE, readLine)
    }

    override fun close() {
        if (::inputStream.isInitialized) {
            inputStream.close()
        }
    }
}