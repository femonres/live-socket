package com.whitecloud.socket.client.managers

import com.whitecloud.socket.client.ConnectionInfo
import com.whitecloud.socket.client.LiveSocketOptions

import java.util.HashMap

class ManagerHolder private constructor() {

    private val connectionManagerMap = HashMap<ConnectionInfo, IConnectionManager>()

    private object InstanceHolder { val INSTANCE = ManagerHolder() }

    init { connectionManagerMap.clear() }

    fun getConnection(info: ConnectionInfo): IConnectionManager {
        val manager = connectionManagerMap[info]
        return if (manager != null) {
            getConnection(info, manager.getOptions())
        } else {
            getConnection(info, LiveSocketOptions.default)
        }
    }

    fun getConnection(info: ConnectionInfo, socketOptions: LiveSocketOptions): IConnectionManager {
        val manager = connectionManagerMap[info]
        if (manager != null) {
            if (socketOptions.isConnectionHolden) {
                synchronized(connectionManagerMap) {
                    connectionManagerMap.remove(info)
                }
                return createNewManagerAndCache(info, socketOptions)
            } else {
                manager.setOptions(socketOptions)
            }
            return manager
        } else {
            return createNewManagerAndCache(info, socketOptions)
        }
    }

    private fun createNewManagerAndCache(info: ConnectionInfo, socketOptions: LiveSocketOptions): IConnectionManager {
        val manager = ConnectionManager(info)
        manager.setOptions(socketOptions)
        synchronized(connectionManagerMap) {
            connectionManagerMap.put(info, manager)
        }
        return manager
    }

    companion object {

        val instance: ManagerHolder
            get() = InstanceHolder.INSTANCE
    }
}
