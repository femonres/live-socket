package com.whitecloud.socket.client.action

import com.whitecloud.socket.client.ConnectionInfo
import com.whitecloud.socket.core.interfaces.IPulseSendable
import com.whitecloud.socket.core.interfaces.ISendable
import com.whitecloud.socket.core.interfaces.IStateSender

abstract class SocketActionAdapter : ISocketActionListener {

    override fun onSocketIOThreadStart(action: IStateSender.State) {
        //println("SocketActionAdapter.onSocketIOThreadStart -> state = $action")
    }

    override fun onSocketIOThreadShutdown(action: IStateSender.State, e: Exception) {
        //println("SocketActionAdapter.onSocketIOThreadShutdown -> state = $action, ${e.localizedMessage}")
    }

    override fun onSocketConnectionSuccess(info: ConnectionInfo, action: IStateSender.State) {
        //println("SocketActionAdapter.onSocketConnectionSuccess -> state = $action")
    }

    override fun onSocketConnectionFailed(info: ConnectionInfo, action: IStateSender.State, error: Exception) {
        //println("SocketActionAdapter.onSocketConnectionFailed -> state = $action, ${error.localizedMessage}")
    }

    override fun onSocketReadResponse(info: ConnectionInfo, action: IStateSender.State, data: String) {
        //println("SocketActionAdapter.onSocketReadResponse -> state = $action")
    }

    override fun onSocketWriteResponse(info: ConnectionInfo, action: IStateSender.State, data: ISendable) {
        //println("SocketActionAdapter.onSocketWriteResponse -> state = $action")
    }

    override fun onPulseSend(info: ConnectionInfo, data: IPulseSendable) {
        //println("SocketActionAdapter.onPulseSend")
    }

    override fun onSocketDisconnection(info: ConnectionInfo, action: IStateSender.State, error: Exception?) {
        //println("SocketActionAdapter.onSocketDisconnection -> state = $action")
    }
}