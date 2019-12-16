package com.whitecloud.socket.client.action

import com.whitecloud.socket.client.ConnectionInfo
import com.whitecloud.socket.client.managers.IConnectionManager
import com.whitecloud.socket.core.interfaces.AbsLoopThread
import com.whitecloud.socket.core.interfaces.IPulseSendable
import com.whitecloud.socket.core.interfaces.ISendable
import com.whitecloud.socket.core.interfaces.IStateSender
import com.whitecloud.socket.core.interfaces.dispatcher.IRegister
import java.io.Serializable
import java.util.ArrayList
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

class ActionDispatcher(
    private val info: ConnectionInfo,
    private val manager: IConnectionManager
): IRegister<ISocketActionListener, IConnectionManager>, IStateSender {

    @Volatile
    private var responseHandlerList = ArrayList<ISocketActionListener>()

    private val reentrantLock = ReentrantLock(true)

    override fun registerReceiver(socketActionListener: ISocketActionListener): IConnectionManager {
        //println("ActionDispatcher.registerReceiver -> responseHandler is: $socketActionListener")
        try {
            while (true) {
                if (reentrantLock.tryLock(1, TimeUnit.SECONDS)) {
                    if (!responseHandlerList.contains(socketActionListener)) {
                        responseHandlerList.add(socketActionListener)
                    }
                    break
                }
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            reentrantLock.unlock()
        }

        return manager
    }

    override fun unRegisterReceiver(socketActionListener: ISocketActionListener): IConnectionManager {
        //println("ActionDispatcher.unRegisterReceiver -> responseHandler is: $socketActionListener")
        try {
            while (true) {
                if (reentrantLock.tryLock(1, TimeUnit.SECONDS)) {
                    responseHandlerList.remove(socketActionListener)
                    break
                }
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            reentrantLock.unlock()
        }

        return manager
    }

    override fun sendBroadcast(action: IStateSender.State, serializable: Serializable?) {
        val actionBean = ActionBean(this, action, serializable)
        BLOCKING_QUEUE.offer(actionBean)
    }

    private fun dispatchActionToListener(action: IStateSender.State, arg: Serializable?, responseHandler: ISocketActionListener) {
        println("ActionDispatcher.dispatchActionToListener -> action: $action, handler = $responseHandler")
        try {
            when (action) {
                IStateSender.State.ACTION_CONNECTION_SUCCESS -> responseHandler.onSocketConnectionSuccess(info, action)
                IStateSender.State.ACTION_CONNECTION_FAILED -> responseHandler.onSocketConnectionFailed(info, action, arg as Exception)
                IStateSender.State.ACTION_READ_THREAD_START -> responseHandler.onSocketIOThreadStart(action)
                IStateSender.State.ACTION_READ_THREAD_SHUTDOWN -> responseHandler.onSocketIOThreadShutdown(action, arg as Exception)
                IStateSender.State.ACTION_READ_COMPLETE -> responseHandler.onSocketReadResponse(info, action, arg as String)
                IStateSender.State.ACTION_WRITE_THREAD_START -> responseHandler.onSocketIOThreadStart(action)
                IStateSender.State.ACTION_WRITE_THREAD_SHUTDOWN -> responseHandler.onSocketIOThreadShutdown(action, arg as Exception)
                IStateSender.State.ACTION_WRITE_COMPLETE -> responseHandler.onSocketWriteResponse(info, action, arg as ISendable)
                IStateSender.State.ACTION_PULSE_REQUEST -> responseHandler.onPulseSend(info, arg as IPulseSendable)
                IStateSender.State.ACTION_DISCONNECTION -> responseHandler.onSocketDisconnection(info, action, arg as Exception)
            }
        } catch (e: Exception) {
            println("ActionDispatcher.dispatchActionToListener -> error: ${e.localizedMessage}")
            e.printStackTrace()
        }
    }

    private data class ActionBean(val dispatcher: ActionDispatcher, val action: IStateSender.State, val arg: Serializable? = null)

    private class DispatchThread: AbsLoopThread() {

        @Throws(Exception::class)
        override fun runInLoopThread() {
            val actionBean = BLOCKING_QUEUE.take()
            val actionDispatcher = actionBean.dispatcher
            synchronized(actionDispatcher.responseHandlerList) {
                val copyData = ArrayList(actionDispatcher.responseHandlerList)
                for (listener in copyData) {
                    actionDispatcher.dispatchActionToListener(actionBean.action, actionBean.arg, listener)
                }
            }
        }

        override fun loopFinish(e: java.lang.Exception?) {
            println("DispatchThread.loopFinish")
        }
    }

    companion object {

        private val BLOCKING_QUEUE = LinkedBlockingQueue<ActionBean>()

        private val HANDLE_THREAD = DispatchThread()

        init { HANDLE_THREAD.start() }
    }
}