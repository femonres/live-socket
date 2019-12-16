package com.whitecloud.livesocket.model

import com.whitecloud.socket.core.interfaces.ISendable
import java.nio.charset.Charset

data class MessageBean(val data: String) : ISendable {

    override fun parse(): ByteArray {

        return data.toByteArray(Charset.defaultCharset())
    }
}