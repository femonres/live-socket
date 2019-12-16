package com.whitecloud.socket.client.managers

import com.whitecloud.socket.client.ConnectionInfo
import com.whitecloud.socket.core.interfaces.IStateSender

class NoneReconnect : AbsReconnectionManager() {
    override fun onSocketConnectionSuccess(info: ConnectionInfo, action: IStateSender.State) {}

    override fun onSocketConnectionFailed(info: ConnectionInfo, action: IStateSender.State, error: Exception) {}

    override fun onSocketDisconnection(info: ConnectionInfo, action: IStateSender.State, error: Exception?) {}
}