package com.whitecloud.socket.client.action

import com.whitecloud.socket.client.ConnectionInfo
import com.whitecloud.socket.core.interfaces.IPulseSendable
import com.whitecloud.socket.core.interfaces.ISendable
import com.whitecloud.socket.core.interfaces.IStateSender

interface ISocketActionListener {

    fun onSocketIOThreadStart(action: IStateSender.State)

    fun onSocketIOThreadShutdown(action: IStateSender.State, e: Exception)

    fun onSocketConnectionSuccess(info: ConnectionInfo, action: IStateSender.State)

    fun onSocketConnectionFailed(info: ConnectionInfo, action: IStateSender.State, error: Exception)

    fun onSocketReadResponse(info: ConnectionInfo, action: IStateSender.State, data: String)

    fun onSocketWriteResponse(info: ConnectionInfo, action: IStateSender.State, data: ISendable)

    fun onPulseSend(info: ConnectionInfo, data: IPulseSendable)

    fun onSocketDisconnection(info: ConnectionInfo, action: IStateSender.State, error: Exception?)
}