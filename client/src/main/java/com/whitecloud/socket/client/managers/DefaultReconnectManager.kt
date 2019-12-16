package com.whitecloud.socket.client.managers

import com.whitecloud.socket.client.ConnectionInfo
import com.whitecloud.socket.core.exceptions.ManuallyDisconnectException
import com.whitecloud.socket.core.interfaces.AbsLoopThread
import com.whitecloud.socket.core.interfaces.IStateSender
import com.whitecloud.socket.core.thread.ThreadUtils


class DefaultReconnectManager : AbsReconnectionManager() {

    private var connectionFailedTimes = 0

    private val maxConnectionFailedTimes = 10

    @Volatile
    private var reconnectTestingThread: ReconnectTestingThread?

    init {
        println("DefaultReconnectManager.init")
        reconnectTestingThread = ReconnectTestingThread()
    }

    override fun onSocketConnectionSuccess(info: ConnectionInfo, action: IStateSender.State) {
        println("DefaultReconnectManager.onSocketConnectionSuccess")
        resetThread()
    }

    override fun onSocketConnectionFailed(info: ConnectionInfo, action: IStateSender.State, error: Exception) {
        println("DefaultReconnectManager.onSocketConnectionFailed -> status: $action, error: ${error.localizedMessage}")
        connectionFailedTimes++

        if (connectionFailedTimes > maxConnectionFailedTimes) {
            resetThread()
            //TODO: Prepare switch to the backup connection
            println("DefaultReconnectManager.onSocketConnectionFailed -> switchConnectionInfo")
        } else {
            println("DefaultReconnectManager.onSocketConnectionFailed -> reconnectDelay")
            reconnectDelay()
        }
    }

    override fun onSocketDisconnection(info: ConnectionInfo, action: IStateSender.State, error: Exception?) {
        println("DefaultReconnectManager.onSocketDisconnection -> error: $error")
        if (isNeedReconnect(error)) {
            reconnectDelay()
        } else {
            resetThread()
        }
    }

    private fun isNeedReconnect(error: Exception?): Boolean {
        println("DefaultReconnectManager.isNeedReconnect -> error: ${error?.localizedMessage}")
        synchronized(ignoreDisconnectExceptionList) {
            if (error != null && error !is ManuallyDisconnectException) {
                for (classException in ignoreDisconnectExceptionList) {
                    if (classException.isAssignableFrom(error.javaClass))
                        return false
                }

                return true
            }

            return false
        }
    }

    @Synchronized
    private fun resetThread() {
        reconnectTestingThread?.shutdown()
    }

    private fun reconnectDelay() {
        reconnectTestingThread?.let { thread ->
            synchronized(thread) {
                if (thread.isShutdown) {
                    thread.start()
                }
            }
        }
    }

    private inner class ReconnectTestingThread : AbsLoopThread() {

        private var reconnectTimeDelay = 10_000

        @Throws(Exception::class)
        override fun beforeLoop() {
            super.beforeLoop()
            if (reconnectTimeDelay < connectionManager.getOptions().connectTimeout) {
                reconnectTimeDelay = connectionManager.getOptions().connectTimeout
            }
        }

        override fun runInLoopThread() {
            if (isDetach) {
                println("ReconnectTestingThread.runInLoopThread -> ReconnectionManager already detached by framework.We decide gave up this reconnection mission!")
                shutdown()
                return
            }

            println("ReconnectTestingThread.runInLoopThread -> Reconnect after $reconnectTimeDelay mills ...")
            ThreadUtils.sleep(reconnectTimeDelay.toLong())

            if (isDetach) {
                println("ReconnectTestingThread.runInLoopThread -> ReconnectionManager already detached by framework.We decide gave up this reconnection mission!")
                shutdown()
                return
            }

            if (connectionManager.isConnect()) {
                shutdown()
                return
            }

            println("ReconnectTestingThread.runInLoopThread -> Reconnect the server ${connectionManager.getRemoteConnectionInfo()} ....")
            synchronized(connectionManager) {
                if (connectionManager.isConnect()) {
                    println("ReconnectTestingThread.runInLoopThread -> is connect")
                    shutdown()
                } else {
                    println("ReconnectTestingThread.runInLoopThread -> is disconnect")
                    connectionManager.connect()
                }
            }
        }

        override fun loopFinish(e: java.lang.Exception?) {
            println("ReconnectTestingThread.loopFinish")
        }
    }

}
