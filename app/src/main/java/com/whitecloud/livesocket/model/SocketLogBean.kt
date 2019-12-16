package com.whitecloud.livesocket.model

import java.text.SimpleDateFormat
import java.util.*

data class SocketLogBean(private val unixTime: Long, val data: String, val who: String) {
    var time: String

    init {
        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        time = format.format(unixTime)
    }
}