package com.whitecloud.socket.client

import javax.net.ssl.KeyManager
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager


class LiveSocketSSLConfig private constructor() {

    var protocol: String? = null
        private set

    var trustManagers: Array<TrustManager>? = null
        private set

    var keyManagers: Array<KeyManager>? = null
        private set

    var customSSLFactory: SSLSocketFactory? = null
        private set

    class Builder {

        private val mConfig: LiveSocketSSLConfig = LiveSocketSSLConfig()

        fun setProtocol(protocol: String): Builder {
            mConfig.protocol = protocol
            return this
        }

        fun setTrustManagers(trustManagers: Array<TrustManager>): Builder {
            mConfig.trustManagers = trustManagers
            return this
        }

        fun setKeyManagers(keyManagers: Array<KeyManager>): Builder {
            mConfig.keyManagers = keyManagers
            return this
        }

        fun setCustomSSLFactory(customSSLFactory: SSLSocketFactory): Builder {
            mConfig.customSSLFactory = customSSLFactory
            return this
        }

        fun build(): LiveSocketSSLConfig {
            return mConfig
        }
    }
}
