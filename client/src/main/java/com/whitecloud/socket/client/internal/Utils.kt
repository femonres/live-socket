package com.whitecloud.socket.client.internal

import java.util.concurrent.TimeUnit

object Utils {

    fun checkDuration(name: String, duration: Long, unit: TimeUnit?): Int {
        require(duration >= 0) { "$name < 0" }
        if (unit == null) throw NullPointerException("unit == null")
        val millis = unit.toMillis(duration)
        require(millis <= Integer.MAX_VALUE) { "$name too large." }
        require(!(millis == 0L && duration > 0)) { "$name too small." }
        return millis.toInt()
    }
}