package com.whitecloud.socket.core.interfaces

import java.lang.Exception


abstract class AbsLoopThread : Runnable {

    @Volatile
    private var thread: Thread? = null

    @Volatile
    private var ioException: Exception? = null

    @Volatile
    private var isStop = true

    @Volatile
    var isShutdown = true
        private set

    @Volatile
    var loopTimes: Int = 0
        private set


    override fun run() {
        try {
            isShutdown = false
            beforeLoop()
            while (!isStop) {
                this.runInLoopThread()
                loopTimes++
            }
        } catch (e: Exception) {
            ioException = e
        } finally {
            isShutdown = true
            this.loopFinish(ioException)
            ioException = null
            println("AbsLoopThread.run -> ${thread?.name}:${thread?.id} is shutting down")
        }
    }

    @Synchronized
    fun start() {
        if (isStop) {
            thread = Thread(this, this.javaClass.simpleName)
            isStop = false
            loopTimes = 0
            thread?.start()
            println("AbsLoopThread.start -> ${thread?.name}:${thread?.id} is Starting")
        }
    }

    @Throws(Exception::class)
    protected open fun beforeLoop() {}

    @Throws(Exception::class)
    protected abstract fun runInLoopThread()

    protected abstract fun loopFinish(e: Exception?)

    @Synchronized
    fun shutdown() {
        if (thread != null && !isStop) {
            println("AbsLoopThread.shutdown ${thread?.name}:${thread?.id} is shutdown")
            isStop = true
            thread?.interrupt()
            thread = null
        }
    }

    @Synchronized
    open fun shutdown(exception: Exception) {
        ioException = exception
        shutdown()
    }
}
