package com.blueshark.lightcast.listener

interface AddBookmarkListener {
    fun onBookmarkSuccess()
    fun onBookmarkFailed(message : String?)
}