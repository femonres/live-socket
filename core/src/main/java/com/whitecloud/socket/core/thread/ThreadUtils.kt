package com.whitecloud.socket.core.thread

object ThreadUtils {

    fun sleep(mills: Long) {
        var mills = mills
        var weakTime: Long = 0
        var startTime: Long = 0

        while (true) {
            try {
                if (weakTime - startTime < mills) {
                    mills -= (weakTime - startTime)
                } else {
                    break
                }
                startTime = System.currentTimeMillis()
                Thread.sleep(mills)
                weakTime = System.currentTimeMillis()
            } catch (e: InterruptedException) {
                weakTime = System.currentTimeMillis()
            }

        }
    }
}