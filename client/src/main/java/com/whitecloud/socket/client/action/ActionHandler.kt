package com.whitecloud.socket.client.action

import com.whitecloud.socket.client.ConnectionInfo
import com.whitecloud.socket.client.managers.IConnectionManager
import com.whitecloud.socket.core.exceptions.ManuallyDisconnectException
import com.whitecloud.socket.core.interfaces.IStateSender
import com.whitecloud.socket.core.interfaces.dispatcher.IRegister

class ActionHandler : SocketActionAdapter() {

    private lateinit var connectionManager: IConnectionManager

    private var iOThreadIsCalledDisconnect = false

    fun attach(manager: IConnectionManager, register: IRegister<ISocketActionListener, IConnectionManager>) {
        this.connectionManager = manager
        register.registerReceiver(this)
    }

    fun detach(register: IRegister<ISocketActionListener, IConnectionManager>) {
        register.unRegisterReceiver(this)
    }

    override fun onSocketIOThreadStart(action: IStateSender.State) {
        iOThreadIsCalledDisconnect = false
    }

    override fun onSocketIOThreadShutdown(action: IStateSender.State, e: Exception) {
        if (!iOThreadIsCalledDisconnect) {
            iOThreadIsCalledDisconnect = true
            if (e !is ManuallyDisconnectException) {
                connectionManager.disconnect(e)
            }
        }
    }

    override fun onSocketConnectionFailed(info: ConnectionInfo, action: IStateSender.State, error: Exception) {
        connectionManager.disconnect(error)
    }
}