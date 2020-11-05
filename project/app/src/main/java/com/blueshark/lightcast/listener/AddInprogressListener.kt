package com.blueshark.lightcast.listener

interface AddInprogressListener {
    fun onInprogressSuccess()
    fun onInprogressFailed(message: String?)
}