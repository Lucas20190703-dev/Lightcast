package com.blueshark.lightcast.networking

import android.net.Uri
import com.blueshark.lightcast.listener.RequestDataReceiver
import com.blueshark.lightcast.model.RequestParams
import okhttp3.*
import java.io.IOException

class NewReleaseService : BaseNetWorkService() {
    override fun fetchData(params: Any?, callback: Any?) {
        val parameters = params as RequestParams?
        val url = Uri.Builder().scheme(URL_SCHEME)
            .authority(URL_AUTHORITY)
            .appendPath(URL_PATH_1)
            .appendPath(URL_PATH_2)
            .appendPath(URL_PATH_3)
            .appendQueryParameter(URL_QUERY_PARAM_OFFSET_KEY, parameters?.offset.toString())
            .appendQueryParameter(URL_QUERY_PARAM_SIZE_KEY, parameters?.size.toString())
            .build().toString()

        val request = Request.Builder()
            .headers(headers!!)
            .url(url)
            .build()
        client?.newCall(request)?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                (callback as RequestDataReceiver?)?.onFailed(e)
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    (callback as RequestDataReceiver?)?.onDataReceived(response.body!!.string())
                }
                else {
                    (callback as RequestDataReceiver?)?.onFailed()
                }
            }
        })
    }

    companion object {
        private const val URL_SCHEME = "https"
        private const val URL_AUTHORITY = "api.light.sx"
        private const val URL_PATH_1 = "ext-api"
        private const val URL_PATH_2 = "v1"
        private const val URL_PATH_3 = "new_releases"
        private const val URL_QUERY_PARAM_OFFSET_KEY = "offset"
        private const val URL_QUERY_PARAM_SIZE_KEY = "size"
    }
}