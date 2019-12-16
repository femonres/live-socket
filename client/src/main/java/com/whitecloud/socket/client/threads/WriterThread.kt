package com.whitecloud.socket.client.threads

import com.whitecloud.socket.core.interfaces.AbsLoopThread
import com.whitecloud.socket.core.interfaces.ISocketWriter
import com.whitecloud.socket.core.interfaces.IStateSender
import java.lang.Exception

class WriterThread(private val writer: ISocketWriter, private val stateSender: IStateSender): AbsLoopThread() {

    override fun runInLoopThread() {
        writer.write()
    }

    override fun loopFinish(e: Exception?) {
        if (e != null) {
            //println("WriterThread.loopFinish -> Write error,thread is dead with exception: ${e.localizedMessage}")
        }
        stateSender.sendBroadcast(IStateSender.State.ACTION_WRITE_THREAD_SHUTDOWN, e)
    }

    override fun shutdown(exception: Exception) {
        writer.close()
        super.shutdown(exception)
    }
}