package com.whitecloud.livesocket.model

import com.whitecloud.socket.core.interfaces.ISendable
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset

class LoginBean : ISendable {
    private var content = ""

    init {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("cmd", "19")
            jsonObject.put("tipo", "user_app")
            jsonObject.put("nick", "user_app_11")
            jsonObject.put("correo", "felipe.montoya@whitecloud.com.co")
            jsonObject.put("iniciado", "")
            jsonObject.put("user", "")
            jsonObject.put("calve", "")
            jsonObject.put("veq_id", "")

            jsonObject.put("lat", 0.0)
            jsonObject.put("lng", 0.0)
            jsonObject.put("date_gps", "0")

            content = jsonObject.toString() + "\n"
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun parse(): ByteArray {
        return content.toByteArray(Charset.defaultCharset())
    }
}