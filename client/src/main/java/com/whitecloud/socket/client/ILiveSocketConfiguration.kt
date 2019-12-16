package com.whitecloud.socket.client

import com.whitecloud.socket.client.managers.IConnectionManager

interface ILiveSocketConfiguration {

    fun setOptions(options: LiveSocketOptions): IConnectionManager

    fun getOptions(): LiveSocketOptions
}