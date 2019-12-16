package com.whitecloud.socket.client.threads

import com.whitecloud.socket.core.interfaces.AbsLoopThread
import com.whitecloud.socket.core.interfaces.ISocketReader
import com.whitecloud.socket.core.interfaces.IStateSender
import java.lang.Exception

class ReaderThread(private val reader: ISocketReader, private val stateSender: IStateSender) : AbsLoopThread() {

    override fun runInLoopThread() {
        reader.read()
    }

    override fun loopFinish(e: Exception?) {
        if (e != null) {
            //println("ReaderThread.loopFinish -> Read error, thread is dead with exception: ${e.localizedMessage}")
        }
        stateSender.sendBroadcast(IStateSender.State.ACTION_READ_THREAD_SHUTDOWN, e)
    }

    override fun shutdown(exception: Exception) {
        reader.close()
        super.shutdown(exception)
    }
}