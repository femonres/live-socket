package com.whitecloud.socket.core

import com.whitecloud.socket.core.interfaces.IPulseSendable
import com.whitecloud.socket.core.interfaces.ISendable
import com.whitecloud.socket.core.interfaces.ISocketWriter
import com.whitecloud.socket.core.interfaces.IStateSender
import java.io.OutputStream
import java.util.concurrent.LinkedBlockingQueue

class SocketWriter : ISocketWriter {

    private lateinit var outputStream: OutputStream
    private lateinit var stateSender: IStateSender

    private val queue = LinkedBlockingQueue<ISendable>()

    override fun initialize(outputStream: OutputStream, stateSender: IStateSender) {
        this.outputStream = outputStream
        this.stateSender = stateSender
    }

    override fun write() {
        val sendable = queue.take()

        val sendBytes = sendable.parse()
        outputStream.write(sendBytes)
        outputStream.flush()

        //println("SocketWriter.write -> bytes write length ${sendBytes.size}")

        if (sendable is IPulseSendable) {
            stateSender.sendBroadcast(IStateSender.State.ACTION_PULSE_REQUEST, sendable)
        } else {
            stateSender.sendBroadcast(IStateSender.State.ACTION_WRITE_COMPLETE, sendable)
        }
    }

    override fun offer(sendable: ISendable) {
        queue.offer(sendable)
    }

    override fun close() {
        if (::outputStream.isInitialized) {
            outputStream.close()
        }
    }
}