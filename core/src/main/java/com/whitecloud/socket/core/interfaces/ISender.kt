package com.whitecloud.socket.core.interfaces

interface ISender<T> {

    fun send(sendable: ISendable): T
}