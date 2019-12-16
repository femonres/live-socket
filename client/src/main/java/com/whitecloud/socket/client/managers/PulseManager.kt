package com.whitecloud.socket.client.managers

import com.whitecloud.socket.client.LiveSocketOptions
import com.whitecloud.socket.core.exceptions.DogDeadException
import com.whitecloud.socket.core.interfaces.AbsLoopThread
import com.whitecloud.socket.core.interfaces.IPulse
import com.whitecloud.socket.core.interfaces.IPulseSendable
import java.lang.Exception
import java.util.concurrent.atomic.AtomicInteger

class PulseManager(private val manager: IConnectionManager, socketOptions: LiveSocketOptions): IPulse {

    @Volatile var socketOptions = socketOptions

    private lateinit var sendable: IPulseSendable

    @Volatile
    var isDead = false
        private set

    @Volatile
    private var currentFrequency: Int = 0

    @Volatile
    private var loseTimes = AtomicInteger(-1)

    private val pulseThread = PulseThread()

    @Synchronized
    override fun pulse() {
        println("PulseManager.pulse")
        shutdown()
        updateFrequency()
        if (pulseThread.isShutdown) {
            println("PulseManager.pulse -> start Thread Pulse")
            pulseThread.start()
        }
    }

    @Synchronized
    override fun trigger() {
        println("PulseManager.trigger")
        if (isDead) return

        if (::sendable.isInitialized) {
            manager.send(sendable)
        }
    }

    @Synchronized
    override fun dead() {
        println("PulseManager.dead")
        loseTimes.set(0)
        isDead = true
        shutdown()
    }

    @Synchronized
    override fun feed() {
        println("PulseManager.feed")
        loseTimes.set(-1)
    }

    fun setPulseSendable(sendable: IPulseSendable) {
        this.sendable = sendable

        shutdown()
        updateFrequency()
        if (pulseThread.isShutdown) {
            println("PulseManager.setPulseSendable -> start Thread Pulse")
            pulseThread.start()
        }
    }

    @Synchronized
    private fun updateFrequency() {
        println("PulseManager.updateFrequency")
        currentFrequency = socketOptions.pulseFrequency
        currentFrequency = if (currentFrequency < 1_000) 1_000 else currentFrequency
    }

    private fun shutdown() {
        println("PulseManager.shutdown")
        if (!pulseThread.isShutdown)
            pulseThread.shutdown()
    }

    private inner class PulseThread : AbsLoopThread() {

        override fun runInLoopThread() {
            println("PulseThread.runInLoopThread")
            if (isDead) {
                shutdown()
                return
            }

            if (::sendable.isInitialized) {
                if (socketOptions.pulseFeedLoseTimes != -1 && loseTimes.incrementAndGet() >= socketOptions.pulseFeedLoseTimes) {
                    manager.disconnect(DogDeadException("you need feed dog on time,otherwise he will die"))
                } else {
                    manager.send(sendable)
                }
            }

            Thread.sleep(currentFrequency.toLong())
        }

        override fun loopFinish(e: Exception?) {
            println("PulseThread.loopFinish -> ${e?.localizedMessage}")
        }
    }
}