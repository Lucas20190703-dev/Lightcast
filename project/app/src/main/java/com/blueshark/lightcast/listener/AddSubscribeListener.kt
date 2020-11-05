package com.blueshark.lightcast.listener

interface AddSubscribeListener {
    fun onSubscribeSuccess()
    fun onSubscribeFailed(message : String?)
}