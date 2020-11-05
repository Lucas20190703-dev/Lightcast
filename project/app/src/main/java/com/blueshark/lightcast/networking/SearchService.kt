package com.blueshark.lightcast.networking

import android.net.Uri
import com.blueshark.lightcast.consts.JSON
import com.blueshark.lightcast.listener.RequestDataReceiver
import com.blueshark.lightcast.model.SearchQueryParam
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class SearchService : BaseNetWorkService(){
    override fun fetchData(params: Any?, callback: Any?) {
        val parameters = params as SearchQueryParam?
        val url = Uri.Builder().scheme(URL_SCHEME)
            .authority(URL_AUTHORITY)
            .appendPath(URL_PATH_1)
            .appendPath(URL_PATH_2)
            .appendPath(URL_PATH_3)
            .appendPath(URL_PATH_4)
            .appendQueryParameter(URL_QUERY_PARAM_QUERY_KEY, parameters?.query)
            .build().toString()

        val request = when {
            headers != null -> {
                Request.Builder()
                    .headers(headers!!)
                    .url(url)
                    .build()
            }
            else -> {
                Request.Builder()
                    .url(url)
                    .build()
            }
        }

        client?.newCall(request)?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                (callback as RequestDataReceiver?)?.onFailed(e)
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    //(callback as RequestDataReceiver?)?.onDataReceivedStream(response.body!!.charStream())
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
        private const val URL_PATH_3 = "podcast"
        private const val URL_PATH_4 = "getAll"
        private const val URL_QUERY_PARAM_P_KEY = "p"
        private const val URL_QUERY_PARAM_QUERY_KEY = "q"
        private const val URL_QUERY_PARAM_A_KEY = "a"
        private const val URL_QUERY_PARAM_T_KEY = "t"
        private const val URL_QUERY_PARAM_W_KEY = "w"
    }
}