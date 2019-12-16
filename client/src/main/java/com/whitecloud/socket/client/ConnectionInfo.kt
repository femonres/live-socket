package com.whitecloud.socket.client

import java.io.Serializable

class ConnectionInfo(val ipAddress: String, val port: Int): Serializable, Cloneable {

    private var backupInfo: ConnectionInfo? = null

    fun setBackupInfo(connectionInfo: ConnectionInfo) {
        this.backupInfo = connectionInfo
    }

    fun isValidInfo(): Boolean {
        return !ipAddress.isBlank()
    }

    public override fun clone(): ConnectionInfo {
        val connectionInfo = ConnectionInfo(ipAddress, port)
        if (backupInfo != null) {
            connectionInfo.setBackupInfo(backupInfo!!.clone())
        }
        return connectionInfo
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConnectionInfo) return false

        return if (port != other.port) {
            false
        } else ipAddress == other.ipAddress
    }

    override fun hashCode(): Int {
        var result = ipAddress.hashCode()
        result = 31 * result + port

        return result
    }

    override fun toString(): String {
        return "$ipAddress:$port"
    }
}