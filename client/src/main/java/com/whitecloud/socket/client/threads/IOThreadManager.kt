package com.whitecloud.socket.client.threads

import com.whitecloud.socket.core.exceptions.ManuallyDisconnectException
import com.whitecloud.socket.core.SocketReader
import com.whitecloud.socket.core.SocketWriter
import com.whitecloud.socket.core.interfaces.*
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception

class IOThreadManager(
    private val inputStream: InputStream,
    private val outputStream: OutputStream,
    private val stateSender: IStateSender
): IIOManager {

    private var ioReader: ISocketReader = SocketReader()
    private var ioWriter: ISocketWriter = SocketWriter()

    private var ioReaderThread: ReaderThread? = null
    private var ioWriterThread: WriterThread? = null

    @Synchronized
    override fun startEngine() {
        ioReader.initialize(inputStream, stateSender)
        ioReaderThread = ReaderThread(ioReader, stateSender)
        ioReaderThread?.start()

        ioWriter.initialize(outputStream, stateSender)
        ioWriterThread = WriterThread(ioWriter, stateSender)
        ioWriterThread?.start()
    }

    override fun send(sendable: ISendable) {
        ioWriter.offer(sendable)
    }

    @Synchronized
    override fun close(error: Exception) {
        shutdownAllThread(error)
    }

    @Synchronized
    override fun close() {
        close(ManuallyDisconnectException())
    }

    private fun shutdownAllThread(error: Exception) {
        //println("IOThreadManager.shutdownAllThread -> Shutdown All thread whit error: ${error.localizedMessage}")
        if (ioReaderThread != null) {
            ioReaderThread?.shutdown(error)
            ioReaderThread = null
        }
        if (ioWriterThread != null) {
            ioWriterThread?.shutdown(error)
            ioWriterThread = null
        }
    }
}