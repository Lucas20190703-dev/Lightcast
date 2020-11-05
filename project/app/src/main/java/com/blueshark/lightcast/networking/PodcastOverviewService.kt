package com.blueshark.lightcast.networking

import android.net.Uri
import com.blueshark.lightcast.consts.JSON
import com.blueshark.lightcast.listener.RequestDataReceiver
import com.blueshark.lightcast.model.PodcastRequestParams
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class PodcastOverviewService : BaseNetWorkService() {

    override fun fetchData(params: Any?, callback: Any?) {
        val requestParam = params as PodcastRequestParams
        val podcastId = requestParam.podcastId
        val offset = requestParam.offset
        val size = requestParam.size
        val url = Uri.Builder().scheme(URL_SCHEME)
            .authority(URL_AUTHORITY)
            .appendPath(URL_PATH_1)
            .appendPath(URL_PATH_2)
            .appendPath(URL_PATH_3)
            .appendPath(URL_API)
            .appendQueryParameter(URL_QUERY_PARAM_PODCAST_ID, podcastId.toString())
            .appendQueryParameter(URL_QUERY_PARAM_PODCAST_SUMMARY_REQUIRED, false.toString())
            .appendQueryParameter(URL_QUERY_PARAM_OFFSET_KEY, size.toString())
            .build().toString()

        val body: RequestBody = "".toRequestBody(JSON)

        val request = Request.Builder()
            .headers(headers!!)
            .url(url)
            .post(body)
            .build()

        client?.newCall(request)?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                (callback as RequestDataReceiver?)?.onFailed(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    //(callback as RequestDataReceiver?)?.onDataReceived(response.body!!.string())
                    (callback as RequestDataReceiver?)?.onDataReceivedStream(response.body!!.charStream())
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
        private const val URL_API = "getPodcastDetail"
        private const val URL_QUERY_PARAM_PODCAST_ID = "podcastId"
        private const val URL_QUERY_PARAM_PODCAST_SUMMARY_REQUIRED = "episodeSummaryRequired"
        private const val URL_QUERY_PARAM_OFFSET_KEY = "offset"
    }
}