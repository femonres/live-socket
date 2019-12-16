package com.whitecloud.socket.client.managers

import com.whitecloud.socket.client.ConnectionInfo
import com.whitecloud.socket.client.action.ActionDispatcher
import com.whitecloud.socket.client.action.ISocketActionListener
import com.whitecloud.socket.core.interfaces.IPulseSendable
import com.whitecloud.socket.core.interfaces.ISendable
import com.whitecloud.socket.core.interfaces.IStateSender
import java.io.Serializable
import java.util.LinkedHashSet


abstract class AbsReconnectionManager : ISocketActionListener {

    // protected var pulseManager: PulseManager? = null //TODO: Verificar si es necesario el pulseManager

    @Volatile
    protected lateinit var connectionManager: IConnectionManager

    @Volatile
    protected var ignoreDisconnectExceptionList: MutableSet<Class<out Exception>> = LinkedHashSet()

    @Volatile
    protected var isDetach: Boolean = false

    @Synchronized
    fun attach(manager: IConnectionManager) {
        if (isDetach) detach()

        isDetach = false
        connectionManager = manager
        //pulseManager = manager.getPulseManager()
        connectionManager.registerReceiver(this)
    }

    @Synchronized
    fun detach() {
        isDetach = true

        if (::connectionManager.isInitialized)
            connectionManager.unRegisterReceiver(this)
    }

    fun addIgnoreException(e: Class<out Exception>) {
        synchronized(ignoreDisconnectExceptionList) {
            ignoreDisconnectExceptionList.add(e)
        }
    }

    fun removeIgnoreException(e: Exception) {
        synchronized(ignoreDisconnectExceptionList) {
            ignoreDisconnectExceptionList.remove(e.javaClass)
        }
    }

    fun removeIgnoreException(e: Class<out Exception>) {
        synchronized(ignoreDisconnectExceptionList) {
            ignoreDisconnectExceptionList.remove(e)
        }
    }

    fun removeAll() {
        synchronized(ignoreDisconnectExceptionList) {
            ignoreDisconnectExceptionList.clear()
        }
    }

    override fun onSocketIOThreadStart(action: IStateSender.State) {}

    override fun onSocketIOThreadShutdown(action: IStateSender.State, e: Exception) {}

    override fun onSocketReadResponse(info: ConnectionInfo, action: IStateSender.State, data: String) {}

    override fun onSocketWriteResponse(info: ConnectionInfo, action: IStateSender.State, data: ISendable) {}

    override fun onPulseSend(info: ConnectionInfo, data: IPulseSendable) {}
}
