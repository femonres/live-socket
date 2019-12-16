package com.whitecloud.socket.client.managers

import com.whitecloud.socket.client.ConnectionInfo
import com.whitecloud.socket.client.LiveSocketOptions
import com.whitecloud.socket.client.action.ActionDispatcher
import com.whitecloud.socket.client.action.ActionHandler
import com.whitecloud.socket.client.action.ISocketActionListener
import com.whitecloud.socket.core.exceptions.ManuallyDisconnectException
import com.whitecloud.socket.client.threads.IOThreadManager
import com.whitecloud.socket.core.exceptions.UnConnectException
import com.whitecloud.socket.core.interfaces.IIOManager
import com.whitecloud.socket.core.interfaces.ISendable
import com.whitecloud.socket.core.interfaces.IStateSender
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket


class ConnectionManager(private val connectionInfo: ConnectionInfo): IConnectionManager {

    @Volatile
    private lateinit var socket: Socket

    @Volatile
    private var pulseManager: PulseManager? = null

    @Volatile
    private lateinit var reconnectionManager: AbsReconnectionManager

    @Volatile
    private lateinit var socketOptions: LiveSocketOptions

    private lateinit var ioManager: IIOManager

    private lateinit var connectThread: Thread

    private lateinit var actionHandler: ActionHandler

    private var actionDispatcher = ActionDispatcher(connectionInfo, this)

    @Volatile
    private var isConnectionPermitted = true

    @Volatile
    private var isDisconnecting = false

    override fun isConnect(): Boolean {
        if (::socket.isInitialized) {
            return socket.isConnected && !socket.isClosed
        }

        return false
    }

    override fun registerReceiver(socketActionListener: ISocketActionListener): IConnectionManager {
        actionDispatcher.registerReceiver(socketActionListener)

        return this
    }

    override fun unRegisterReceiver(socketActionListener: ISocketActionListener): IConnectionManager {
        actionDispatcher.unRegisterReceiver(socketActionListener)

        return this
    }

    override fun getPulseManager(): PulseManager? {
        return pulseManager
    }

    override fun getRemoteConnectionInfo(): ConnectionInfo {
        return connectionInfo.clone()
    }

    override fun getReconnectionManager(): AbsReconnectionManager {
        return socketOptions.reconnectionManager!!
    }

    @Synchronized
    override fun connect() {
        val currentThread = Thread.currentThread()
        println("ConnectionManager.connect -> Thread name ${currentThread.name}:${currentThread.id}")
        if (!isConnectionPermitted) return
        isConnectionPermitted = false

        if (isConnect()) return
        isDisconnecting = false
        if (!connectionInfo.isValidInfo()) {
            throw UnConnectException("The connection parameters are empty, check the connection parameters")
        }

        if (::actionHandler.isInitialized) {
            actionHandler.detach(this)
        }
        actionHandler = ActionHandler()
        actionHandler.attach(this, this)
/*
        if (!::reconnectionManager.isInitialized) {
            println("ConnectionManager.connect -> instance ReconnectManager")
            reconnectionManager = socketOptions.reconnectionManager!!
        }
        reconnectionManager.detach()
        reconnectionManager.attach(this)
*/
        connectThread = ConnectionThread("Connect for $connectionInfo")
        connectThread.isDaemon = true
        connectThread.start()
    }

    override fun send(sendable: ISendable): IConnectionManager {
        if (::ioManager.isInitialized && isConnect()) {
            ioManager.send(sendable)
        }

        return this
    }

    override fun disconnect(exception: Exception) {
        synchronized(this) {
            if (isDisconnecting) return
            isDisconnecting = true

            if (pulseManager != null) {
                println("ConnectionManager.disconnect -> Terminate pulseManager")
                pulseManager!!.dead()
                pulseManager = null
            }
        }

        if (exception is ManuallyDisconnectException) {
            if (::reconnectionManager.isInitialized) {
                reconnectionManager.detach()
            }
        }

        val disconnectThread = DisconnectThread(exception, "Disconnect Thread for $connectionInfo")
        disconnectThread.isDaemon = true
        disconnectThread.start()
    }

    override fun disconnect() {
        disconnect(ManuallyDisconnectException())
    }

    override fun setOptions(options: LiveSocketOptions): IConnectionManager {
        socketOptions = options

        if (pulseManager != null) {
            pulseManager!!.socketOptions = socketOptions
        }

        if (::reconnectionManager.isInitialized && reconnectionManager == socketOptions.reconnectionManager!!) {
            reconnectionManager.detach()

            reconnectionManager = socketOptions.reconnectionManager!!
            reconnectionManager.attach(this)
        }

        return this
    }

    override fun getOptions(): LiveSocketOptions {
        return socketOptions
    }


    @Synchronized
    @Throws(Exception::class)
    fun buildSocketByConfig(): Socket {
        if (socketOptions.socketFactory != null) {
            return socketOptions.socketFactory!!.createSocket(connectionInfo, socketOptions)
        }

        //TODO: Create socket with SSL Support

        return Socket()
    }

    @Throws(IOException::class)
    private fun resolveManager() {
        pulseManager = PulseManager(this, socketOptions)

        ioManager = IOThreadManager(socket.getInputStream(), socket.getOutputStream(), actionDispatcher)
        ioManager.startEngine()
    }

    private fun sendBroadcast(action: IStateSender.State, error: Exception? = null) {
        actionDispatcher.sendBroadcast(action, error)
    }

    private inner class ConnectionThread(name: String): Thread(name) {

        override fun run() {
            try {
                println("ConnectionThread.run -> Start connect to server: $connectionInfo")
                val socketAddress: InetSocketAddress
                try {
                    socketAddress = InetSocketAddress(connectionInfo.ipAddress, connectionInfo.port)
                } catch (e: Exception) {
                    throw UnConnectException(e)
                }

                try {
                    this@ConnectionManager.socket = buildSocketByConfig()
                } catch (e: Exception) {
                    throw UnConnectException(e)
                }

                //TODO: Manage backup ConnectionInfo to Reconnect

                this@ConnectionManager.socket.connect(socketAddress, socketOptions.connectTimeout)
                this@ConnectionManager.socket.tcpNoDelay = true

                resolveManager()
                println("ConnectionThread.run -> Connect successful!")
                sendBroadcast(IStateSender.State.ACTION_CONNECTION_SUCCESS)
            } catch (e: Exception) {
                println("ConnectionThread.run -> Connect failed! error msg: ${e.localizedMessage}")
                sendBroadcast(IStateSender.State.ACTION_CONNECTION_FAILED, UnConnectException(e))
                // e.printStackTrace()
            } finally {
                isConnectionPermitted = true
            }
        }
    }

    private inner class DisconnectThread(private val exception: Exception, name: String): Thread(name) {

        override fun run() {
            try {
                if (::ioManager.isInitialized) {
                    println("DisconnectThread.run -> close ioManager")
                    ioManager.close(exception)
                }

                if (::connectThread.isInitialized && connectThread.isAlive) {
                    connectThread.interrupt()
                    try {
                        println("DisconnectThread.run -> disconnect thread need waiting for connection thread done.")
                        connectThread.join()
                    } catch (e: InterruptedException) {}
                    println("DisconnectThread.run -> Connection thread is done. disconnection thread going on")
                }

                if (::socket.isInitialized) {
                    try {
                        socket.close()
                    } catch (e: IOException) {}
                }

                if (::actionHandler.isInitialized)
                    println("DisconnectThread.run -> ActionHandler is detached")
                    actionHandler.detach(this@ConnectionManager)
            } finally {
                isDisconnecting = false
                isConnectionPermitted = true
                println("DisconnectThread.run -> Finish socket")
                if (exception !is UnConnectException) {
                    if (exception is ManuallyDisconnectException) {
                        sendBroadcast(IStateSender.State.ACTION_DISCONNECTION)
                    } else {
                        sendBroadcast(IStateSender.State.ACTION_DISCONNECTION, exception)
                    }
                }

            }
        }
    }
}