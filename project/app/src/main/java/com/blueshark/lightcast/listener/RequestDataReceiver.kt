package com.blueshark.lightcast.listener

import java.io.Reader


interface RequestDataReceiver {
    fun onDataReceived(jsonString : String)
    fun onDataReceivedStream(reader : Reader)
    fun onFailed(t: Throwable ? = Throwable())
}