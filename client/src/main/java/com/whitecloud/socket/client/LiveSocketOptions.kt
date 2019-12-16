package com.whitecloud.socket.client

import com.whitecloud.socket.client.internal.Utils
import com.whitecloud.socket.client.managers.AbsReconnectionManager
import com.whitecloud.socket.client.managers.NoneReconnect
import java.util.concurrent.TimeUnit

class LiveSocketOptions private constructor() {

    var isConnectionHolden: Boolean = false
        private set

    var pulseFrequency: Int = 0
        private set

    var pulseFeedLoseTimes: Int = 0
        private set

    var connectTimeout: Int = 0
        private set

    var reconnectionManager: AbsReconnectionManager? = null
        private set

    var sslConfig: LiveSocketSSLConfig? = null
        private set

    var socketFactory: LiveSocketFactory? = null
        private set

    var isCallbackInIndependentThread: Boolean = false
        private set


    class Builder {
        private val socketOptions: LiveSocketOptions

        constructor(configuration: ILiveSocketConfiguration) : this(configuration.getOptions())

        @JvmOverloads
        constructor(okOptions: LiveSocketOptions = default) {
            socketOptions = okOptions
        }

        fun sslConfig(SSLConfig: LiveSocketSSLConfig): Builder {
            socketOptions.sslConfig = SSLConfig
            return this
        }

        fun connectionHolden(connectionHolden: Boolean): Builder {
            socketOptions.isConnectionHolden = connectionHolden
            return this
        }

        fun pulseFrequency(pulseFrequency: Long, unit: TimeUnit = TimeUnit.SECONDS): Builder {
            socketOptions.pulseFrequency = Utils.checkDuration("pulseFrequency", pulseFrequency, unit)
            return this
        }

        fun pulseFeedLoseTimes(loseTimes: Long, unit: TimeUnit = TimeUnit.SECONDS): Builder {
            socketOptions.pulseFeedLoseTimes = Utils.checkDuration("loseTimes", loseTimes, unit)
            return this
        }

        fun connectTimeout(timeout: Long, unit: TimeUnit =  TimeUnit.SECONDS): Builder {
            socketOptions.connectTimeout = Utils.checkDuration("timeout", timeout, unit)
            return this
        }

        fun reconnectionManager(reconnectionManager: AbsReconnectionManager): Builder {
            socketOptions.reconnectionManager = reconnectionManager
            return this
        }

        fun socketFactory(factory: LiveSocketFactory): Builder {
            socketOptions.socketFactory = factory
            return this
        }

        fun build(): LiveSocketOptions {
            return socketOptions
        }
    }

    companion object {

        var isDebug: Boolean = false

        val default: LiveSocketOptions
            get() {
                val socketOptions = LiveSocketOptions()
                socketOptions.isConnectionHolden = true
                socketOptions.pulseFrequency = 35_000
                socketOptions.connectTimeout = 10_000
                socketOptions.pulseFeedLoseTimes = 50_000
                socketOptions.reconnectionManager = NoneReconnect()
                socketOptions.sslConfig = null
                socketOptions.socketFactory = null
                socketOptions.isCallbackInIndependentThread = true
                return socketOptions
            }
    }

}