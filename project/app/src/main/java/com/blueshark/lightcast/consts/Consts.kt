package com.blueshark.lightcast.consts

import okhttp3.MediaType.Companion.toMediaTypeOrNull


//Request
const val XAccessTokenKey = "X-ACCESS-TOKEN"
const val ContentTypeKey = "Content-Type"

val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()


//const value
const val REFRESH_LOADING_TIME = 3000L
const val DISTANCE_TRIGGER_REFRESH = 400




