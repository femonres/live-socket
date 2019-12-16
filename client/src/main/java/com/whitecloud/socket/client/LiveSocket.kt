package com.whitecloud.socket.client

import com.whitecloud.socket.client.managers.IConnectionManager
import com.whitecloud.socket.client.managers.ManagerHolder

class LiveSocket {

    var connectionInfo: ConnectionInfo? = null
        private set

    var socketOptions: LiveSocketOptions? = null
        private set

    fun create(): IConnectionManager {
        if (connectionInfo != null && connectionInfo!!.isValidInfo()) {
            return if (socketOptions == null) {
                holder.getConnection(connectionInfo!!)
            } else {
                holder.getConnection(connectionInfo!!, socketOptions!!)
            }
        }

        throw IllegalStateException("First you must build the client")
    }

    class Builder @JvmOverloads constructor(private val mLiveSocket: LiveSocket = instance) {

        fun setConnectionInfo(serverAddress: String, serverPort: Int): Builder {
            mLiveSocket.connectionInfo = ConnectionInfo(serverAddress, serverPort)
            return this
        }

        fun setConnectionInfo(info: ConnectionInfo): Builder {
            mLiveSocket.connectionInfo = info
            return this
        }

        fun setSocketOptions(socketOptions: LiveSocketOptions): Builder {
            mLiveSocket.socketOptions = socketOptions
            return this
        }

        fun build(): LiveSocket {
            if (mLiveSocket.connectionInfo != null && mLiveSocket.connectionInfo!!.isValidInfo()) {
                return mLiveSocket
            }

            throw IllegalStateException("IP Server address and port is required.")
        }
    }

    companion object {

        private val holder = ManagerHolder.instance

        val instance: LiveSocket
            get() = LiveSocket()
    }
}
