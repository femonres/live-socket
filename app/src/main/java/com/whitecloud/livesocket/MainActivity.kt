package com.whitecloud.livesocket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.whitecloud.livesocket.databinding.ActivityMainBinding
import com.whitecloud.livesocket.model.MessageBean
import com.whitecloud.livesocket.model.SocketLogBean
import com.whitecloud.socket.client.ConnectionInfo
import com.whitecloud.socket.client.LiveSocket
import com.whitecloud.socket.client.LiveSocketOptions
import com.whitecloud.socket.client.action.SocketActionAdapter
import com.whitecloud.socket.client.managers.IConnectionManager
import com.whitecloud.socket.core.interfaces.IPulseSendable
import com.whitecloud.socket.core.interfaces.ISendable
import com.whitecloud.socket.core.interfaces.IStateSender
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private val binding by lazy { DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main) }

    private val socketReceiveLogAdapter = SocketLogAdapter()

    private lateinit var socketOptions: LiveSocketOptions
    private lateinit var tcpClient: IConnectionManager
    private val socketListener = object : SocketActionAdapter() {

        override fun onSocketConnectionSuccess(info: ConnectionInfo, action: IStateSender.State) {
            Log.d("MainActivity", "Connected to Socket $info")

            // tcpClient.send(LoginBean())
            //tcpClient.getPulseManager()?.setPulseSendable(PulseBean())

            binding.isConnect = true
            binding.connectButton.isChecked = true
            //binding.serverIpEditText.isEnabled = false
            //binding.serverPortEditText.isEnabled = false
        }

        override fun onSocketConnectionFailed(info: ConnectionInfo, action: IStateSender.State, error: Exception) {
            println("MainActivity.onSocketConnectionFailed")
            logSocketDataSend("Connection Failed")

            binding.isConnect = false
            binding.connectButton.isChecked = false
            //binding.serverIpEditText.isEnabled = true
            //binding.serverPortEditText.isEnabled = true
        }

        override fun onSocketReadResponse(info: ConnectionInfo, action: IStateSender.State, data: String) {
            // tcpClient.getPulseManager()?.feed()
            logSocketDataReceive(data)
        }

        override fun onSocketWriteResponse(info: ConnectionInfo, action: IStateSender.State, data: ISendable) {
            // tcpClient.getPulseManager()?.pulse()
            logSocketDataSend(String(data.parse(), Charset.forName("utf-8")))
        }

        override fun onPulseSend(info: ConnectionInfo, data: IPulseSendable) {
            logSocketDataSend("Pulse -- ${String(data.parse(), Charset.forName("utf-8"))}")
        }

        override fun onSocketDisconnection(info: ConnectionInfo, action: IStateSender.State, error: Exception?) {
            println("MainActivity.onSocketDisconnection")
            if (error != null) {
                logSocketDataSend("Disconnected with exception ${error.localizedMessage}")
            } else {
                logSocketDataSend("Disconnect Manually")
            }

            binding.isConnect = false
            binding.connectButton.isChecked = false
            //binding.serverIpEditText.isEnabled = true
            //binding.serverPortEditText.isEnabled = true
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "Initialize Activity")
        // binding.serverIpEditText.setText("104.130.135.143")

        binding.receiveList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.receiveList.adapter = socketReceiveLogAdapter

        binding.isConnect = false

        binding.clearLogButton.setOnClickListener { clearSocketLog() }

        binding.messageSendButton.setOnClickListener { sendDataToSocket() }

        binding.connectButton.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> connectSocket()
                false -> disconnectSocket()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::tcpClient.isInitialized) {
            tcpClient.unRegisterReceiver(socketListener)
            tcpClient.disconnect()
        }
    }

    private fun initSocketManager() {

        val serverIp = binding.serverIpEditText.text.toString()
        val serverPort = Integer.parseInt(binding.serverPortEditText.text.toString())
        val connectionInfo = ConnectionInfo(serverIp, serverPort)


        socketOptions = LiveSocketOptions.Builder()
            .pulseFrequency(35L)
            .connectTimeout(10)
            .build()
        tcpClient = LiveSocket.Builder()
            .setConnectionInfo(connectionInfo)
            //.setConnectionInfo(serverIp, serverPort)
            .setSocketOptions(socketOptions)
            .build().create()

        tcpClient.registerReceiver(socketListener)
    }

    private fun connectSocket() {
        initSocketManager()

        if (::tcpClient.isInitialized) {
            if (!tcpClient.isConnect()) {
                tcpClient.connect()
                binding.serverIpEditText.isEnabled = false
                binding.serverPortEditText.isEnabled = false
            }
        } else {
            binding.connectButton.isChecked = false
        }
    }

    private fun disconnectSocket() {
        if (::tcpClient.isInitialized) {
            if (tcpClient.isConnect()) {
                tcpClient.disconnect()
                binding.serverIpEditText.isEnabled = true
                binding.serverPortEditText.isEnabled = true
            }
        }
    }

    private fun sendDataToSocket() {
        if (::tcpClient.isInitialized) {
            val messageToSend = binding.messageSendEditText.text.toString()
            if (messageToSend.isNotEmpty()) {
                tcpClient.send(MessageBean(messageToSend))
                binding.messageSendEditText.setText("")
            }
        }
    }

    private fun clearSocketLog() {
        socketReceiveLogAdapter.dataList.clear()
        socketReceiveLogAdapter.notifyDataSetChanged()
    }

    private fun logSocketDataReceive(data: String) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            val logBean = SocketLogBean(System.currentTimeMillis(), data, "SERVER")
            socketReceiveLogAdapter.dataList.add(0, logBean)
            socketReceiveLogAdapter.notifyDataSetChanged()
        } else {
            val threadName = Thread.currentThread().name
            Handler(Looper.getMainLooper()).post {
                logSocketDataReceive("$threadName (In Thread): $data")
            }
        }
    }

    private fun logSocketDataSend(data: String) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            val logBean = SocketLogBean(System.currentTimeMillis(), data, "CLIENT")
            socketReceiveLogAdapter.dataList.add(0, logBean)
            socketReceiveLogAdapter.notifyDataSetChanged()
        } else {
            val threadName = Thread.currentThread().name
            Handler(Looper.getMainLooper()).post {
                logSocketDataSend("$threadName (In Thread): $data")
            }
        }
    }
}
