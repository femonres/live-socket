package com.whitecloud.socket.client.managers

import com.whitecloud.socket.client.ConnectionInfo
import com.whitecloud.socket.client.ILiveSocketConfiguration
import com.whitecloud.socket.client.action.ISocketActionListener
import com.whitecloud.socket.core.interfaces.ISender
import com.whitecloud.socket.core.interfaces.dispatcher.IRegister

interface IConnectionManager :
        ILiveSocketConfiguration,
        ISender<IConnectionManager>,
        IRegister<ISocketActionListener, IConnectionManager> {

    fun connect()

    fun isConnect(): Boolean

    fun getPulseManager(): PulseManager?

    fun getRemoteConnectionInfo(): ConnectionInfo

    fun getReconnectionManager(): AbsReconnectionManager

    fun disconnect(exception: Exception)

    fun disconnect()
}