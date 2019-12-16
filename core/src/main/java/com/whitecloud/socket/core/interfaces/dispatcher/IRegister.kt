package com.whitecloud.socket.core.interfaces.dispatcher

interface IRegister<T, E> {

    fun registerReceiver(socketActionListener: T): E

    fun unRegisterReceiver(socketActionListener: T): E
}