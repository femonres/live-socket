package com.whitecloud.livesocket.model

import com.whitecloud.socket.core.interfaces.IPulseSendable
import java.nio.charset.Charset

class PulseBean : IPulseSendable {
    private var data: String = "Ping"

    override fun parse(): ByteArray {

        return data.toByteArray(Charset.defaultCharset())
    }
}