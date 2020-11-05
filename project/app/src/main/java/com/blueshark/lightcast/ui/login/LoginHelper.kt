package com.blueshark.lightcast.ui.login

import android.util.Patterns

fun isEmailValid( email: String?) : Boolean {
    return if (email == null) false
    else Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun isPasswordValid(password: String?) : Boolean {
    return if (password == null) false
    else password.length > 5
}