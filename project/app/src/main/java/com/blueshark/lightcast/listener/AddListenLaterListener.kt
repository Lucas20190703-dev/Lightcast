package com.blueshark.lightcast.listener

interface AddListenLaterListener {
    fun onListenLaterSuccess()
    fun onListenLaterFailed(message : String?)
}