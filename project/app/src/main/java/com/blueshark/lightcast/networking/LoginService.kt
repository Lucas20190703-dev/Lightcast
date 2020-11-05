package com.blueshark.lightcast.networking

import android.net.Uri
import com.blueshark.lightcast.consts.JSON
import com.blueshark.lightcast.listener.RequestDataReceiver
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class LoginService {
    private val client : OkHttpClient = OkHttpClient()
    private val url : String = Uri.Builder().scheme(URL_SCHEME)
        .authority(URL_AUTHORITY)
        .appendPath(URL_PATH_1)
        .appendPath(URL_PATH_2)
        .appendPath(URL_PATH_3)
        .appendPath(URL_PATH_4)
        .build().toString()

    fun login(email : String, password : String, callback: RequestDataReceiver?) {
        val json = JSONObject()
        json.put(URL_QUERY_PARAM_EMAIL_KEY, email)
        json.put(URL_QUERY_PARAM_PASSWORD_KEY, password)

        val body: RequestBody = json.toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback?.onFailed(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback?.onDataReceived(response.body!!.string())
                }
                else {
                    callback?.onFailed()
                }
            }
        })
    }

    companion object{
        private const val URL_SCHEME = "https"
        private const val URL_AUTHORITY = "api.light.sx"
        private const val URL_PATH_1 = "ext-api"
        private const val URL_PATH_2 = "v1"
        private const val URL_PATH_3 = "auth"
        private const val URL_PATH_4 = "login"
        private const val URL_QUERY_PARAM_EMAIL_KEY = "email"
        private const val URL_QUERY_PARAM_PASSWORD_KEY = "password"
    }
}