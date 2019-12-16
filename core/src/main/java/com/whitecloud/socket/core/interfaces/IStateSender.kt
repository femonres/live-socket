package com.whitecloud.socket.core.interfaces

import java.io.Serializable

interface IStateSender {

    fun sendBroadcast(action: State, serializable: Serializable? = null)

    enum class State {
        ACTION_READ_COMPLETE,
        ACTION_WRITE_COMPLETE,
        ACTION_PULSE_REQUEST,
        ACTION_READ_THREAD_START,
        ACTION_READ_THREAD_SHUTDOWN,
        ACTION_WRITE_THREAD_START,
        ACTION_WRITE_THREAD_SHUTDOWN,
        ACTION_CONNECTION_SUCCESS,
        ACTION_CONNECTION_FAILED,
        ACTION_DISCONNECTION
    }
}