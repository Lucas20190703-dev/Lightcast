package com.blueshark.lightcast.networking

import android.net.Uri
import android.util.Log
import com.blueshark.lightcast.consts.JSON
import com.blueshark.lightcast.consts.XAccessTokenKey
import com.blueshark.lightcast.listener.RequestDataReceiver
import com.blueshark.lightcast.model.InprogressEpisodeDto
import com.blueshark.lightcast.model.InprogressEpisodeDtoResponse
import com.blueshark.lightcast.utils.Constants.ADD_IN_PROGRESS_INTERVAL
import com.blueshark.lightcast.utils.PreferenceUtil
import com.google.gson.Gson
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit


class AddToListenLaterService(private val callback: Any?) {
    val client = OkHttpClient.Builder()
        .callTimeout(1, TimeUnit.HOURS)
        .readTimeout(1, TimeUnit.HOURS)
        .connectTimeout(1, TimeUnit.HOURS)
        .writeTimeout(1, TimeUnit.HOURS)
        .addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder().build())
        }
        .build()

    fun execute(podcastId : Long, episodeId : String) {
        val token = PreferenceUtil.getInstance().accessToken
        if (token == null) {
            (callback as RequestDataReceiver?)?.onFailed()
            return
        }

        val headers = Headers.Builder()
            .add(XAccessTokenKey, token)
            .build()

        val url = Uri.Builder().scheme(URL_SCHEME)
            .authority(URL_AUTHORITY)
            .appendPath(URL_PATH_1)
            .appendPath(URL_PATH_2)
            .appendPath(URL_PATH_3)
            .appendPath(URL_API)
            .appendQueryParameter("podcastId", podcastId.toString())
            .appendQueryParameter("episodetId", episodeId)
            .build().toString()

        val body: RequestBody = "".toRequestBody(JSON)
        val request = Request.Builder()
            .headers(headers)
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
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
        private const val URL_PATH_3 = "podcast"
        private const val URL_API = "addListenLater"
    }
}

class AddBookmarkService(private val callback: Any?) {
    val client = OkHttpClient.Builder()
        .callTimeout(1, TimeUnit.HOURS)
        .readTimeout(1, TimeUnit.HOURS)
        .connectTimeout(1, TimeUnit.HOURS)
        .writeTimeout(1, TimeUnit.HOURS)
        .addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder().build())
        }
        .build()

    fun execute(podcastId : Long?, episodeId : String?, timestamp: String?) {
        val token = PreferenceUtil.getInstance().accessToken
        if (token == null) {
            (callback as RequestDataReceiver?)?.onFailed()
            return
        }

        val headers = Headers.Builder()
            .add(XAccessTokenKey, token)
            .build()

        val url = Uri.Builder().scheme(URL_SCHEME)
            .authority(URL_AUTHORITY)
            .appendPath(URL_PATH_1)
            .appendPath(URL_PATH_2)
            .appendPath(URL_PATH_3)
            .build().toString()

        val json = JSONObject()
        json.put("podcastId", podcastId)
        json.put("episodeId", episodeId)
        json.put("timestamp", timestamp)

        val body: RequestBody = json.toString().toRequestBody(JSON)
        val request = Request.Builder()
            .headers(headers)
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
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
        private const val URL_PATH_3 = "bookmark"
    }
}

class AddInProgressService(private val callback : Any?) {
    private val client : OkHttpClient = OkHttpClient.Builder()
        .callTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
    private var token : String? = null

    private lateinit var headers : Headers
    private lateinit var url : String

    private val body = "".toRequestBody(JSON)

    private var addCall : Call? = null

    private var prevEpisodeId : String? = null
    private var prevPodcastId : Long? = null
    private var prevDurationLeft : Double = 0.0
    private var prevDuration : Double? = null


    fun execute(podcastId: Long, episodeId: String, durationLeft: Double, duration: Double) {
        if (duration <= 0 || durationLeft <= 0) return

        if (prevPodcastId != null && prevEpisodeId != null &&
            prevPodcastId == podcastId &&
            prevEpisodeId == episodeId &&
            prevDurationLeft > durationLeft - ADD_IN_PROGRESS_INTERVAL / 1000.0 + 1.0) {

            Log.i(this.javaClass.simpleName, "prevPodcastId = $prevPodcastId prevEpisodeId = $prevEpisodeId prevDurationLeft = $prevDurationLeft")
            Log.i(this.javaClass.simpleName, "podcastId = $podcastId episodeId = $episodeId durationLeft = $durationLeft")
            return
        }

        token = PreferenceUtil.getInstance().accessToken

        if (token == null) {
            (callback as RequestDataReceiver?)?.onFailed()
            return
        }

        headers = Headers.Builder()
            .add(XAccessTokenKey, token!!)
            .build()


        url = Uri.Builder().scheme(URL_SCHEME)
            .authority(URL_AUTHORITY)
            .appendPath(URL_PATH_1)
            .appendPath(URL_PATH_2)
            .appendPath(URL_PATH_3)
            .appendPath(URL_PATH_4)
            .appendQueryParameter("podcastId", podcastId.toString())
            .appendQueryParameter("episodetId", episodeId)
            .appendQueryParameter("durationLeft", durationLeft.toString())
            .appendQueryParameter("duration", duration.toString())
            .build().toString()
        Log.i(this.javaClass.simpleName, url)
        val request = Request.Builder()
            .headers(headers)
            .url(url)
            .post(body)
            .build()

        if (addCall != null) {
            addCall?.cancel()
            addCall = null
        }
        addCall = client.newCall(request)

        addCall?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                addCall = null
                (callback as RequestDataReceiver?)?.onFailed(e)
                Log.i(this.javaClass.simpleName, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    addCall = null
                    prevEpisodeId = episodeId
                    prevPodcastId = podcastId
                    prevDuration = duration
                    prevDurationLeft = durationLeft
                    val responseString = response.body!!.string()
                    (callback as RequestDataReceiver?)?.onDataReceived(responseString)
                    Log.i(this.javaClass.simpleName, responseString)
                } else {
                    (callback as RequestDataReceiver?)?.onFailed()
                    addCall = null
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
        private const val URL_PATH_4 = "addInprogress"
    }
}

class AddSubscribeService(private val callback : Any?) {
    val client = OkHttpClient.Builder()
        .callTimeout(1, TimeUnit.HOURS)
        .readTimeout(1, TimeUnit.HOURS)
        .connectTimeout(1, TimeUnit.HOURS)
        .writeTimeout(1, TimeUnit.HOURS)
        .addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder().build())
        }
        .build()

    fun execute(podcastId : Long?) {
        val token = PreferenceUtil.getInstance().accessToken
        if (token == null) {
            (callback as RequestDataReceiver?)?.onFailed()
            return
        }

        val headers = Headers.Builder()
            .add(XAccessTokenKey, token)
            .build()

        val url = Uri.Builder().scheme(URL_SCHEME)
            .authority(URL_AUTHORITY)
            .appendPath(URL_PATH_1)
            .appendPath(URL_PATH_2)
            .appendPath(URL_PATH_3)
            .appendPath(URL_PATH_4)
            .appendQueryParameter("podcastId", podcastId.toString())
            .build().toString()

        val body: RequestBody = "".toRequestBody(JSON)
        val request = Request.Builder()
            .headers(headers)
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
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

    companion object{
        private const val URL_SCHEME = "https"
        private const val URL_AUTHORITY = "api.light.sx"
        private const val URL_PATH_1 = "ext-api"
        private const val URL_PATH_2 = "v1"
        private const val URL_PATH_3 = "podcast"
        private const val URL_PATH_4 = "subscribePodcast"
    }
}

fun logoutAccount(callback: Any?) {
    val client = OkHttpClient.Builder()
        .callTimeout(1, TimeUnit.HOURS)
        .readTimeout(1, TimeUnit.HOURS)
        .connectTimeout(1, TimeUnit.HOURS)
        .writeTimeout(1, TimeUnit.HOURS)
        .addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder().build())
        }
        .build()

    val token = PreferenceUtil.getInstance().accessToken
    if (token == null) {
        (callback as RequestDataReceiver?)?.onFailed()
        return
    }

    val headers = Headers.Builder()
        .add(XAccessTokenKey, token)
        .build()

    val URL_SCHEME = "https"
    val URL_AUTHORITY = "api.light.sx"
    val URL_PATH_1 = "ext-api"
    val URL_PATH_2 = "v1"
    val URL_PATH_3 = "auth"
    val URL_API = "logout"

    val url = Uri.Builder().scheme(URL_SCHEME)
        .authority(URL_AUTHORITY)
        .appendPath(URL_PATH_1)
        .appendPath(URL_PATH_2)
        .appendPath(URL_PATH_3)
        .appendPath(URL_API)
        .build().toString()

    val body: RequestBody = "".toRequestBody(JSON)

    val request = Request.Builder()
        .headers(headers)
        .url(url)
        .post(body)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            (callback as RequestDataReceiver?)?.onFailed(e)
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                PreferenceUtil.getInstance().clearAll()
                (callback as RequestDataReceiver?)?.onDataReceived(response.body!!.string())
            }
            else {
                (callback as RequestDataReceiver?)?.onFailed()
            }
        }
    })
}

fun getPlayTime(episodeId: String) : InprogressEpisodeDto? {
    val client = OkHttpClient.Builder()
        .callTimeout(5, TimeUnit.HOURS)
        .readTimeout(5, TimeUnit.HOURS)
        .connectTimeout(5, TimeUnit.HOURS)
        .writeTimeout(5, TimeUnit.HOURS)
        .build()

    val token = PreferenceUtil.getInstance().accessToken ?: return null

    val headers = Headers.Builder()
        .add(XAccessTokenKey, token)
        .build()

    val url = Uri.Builder().scheme("https")
        .authority("api.light.sx")
        .appendPath("ext-api")
        .appendPath("v1")
        .appendPath("podcast")
        .appendPath("episode")
        .appendPath("playtime")
        .appendQueryParameter("episodeId", episodeId)
        .build().toString()

    val json = JSONObject()
    json.put("episodeId", episodeId)
    val body: RequestBody = json.toString().toRequestBody(JSON)

    val request = Request.Builder()
        .headers(headers)
        .url(url)
        .post(body)
        .build()

    //val response = synchronizeRequest(request, client)  // request two times
    val response = client.newCall(request).execute()
    if (response.isSuccessful) {
        val gson = Gson()
        val inprogressDto = gson.fromJson(response.body!!.string(), InprogressEpisodeDtoResponse::class.java)
        if (inprogressDto.responseMessage == "DATA_FETCHED" &&
                inprogressDto.inprogressEpisodeDto != null) {
            return inprogressDto.inprogressEpisodeDto
        }
    }
    return null
}

fun synchronizeRequest(request: Request, client: OkHttpClient) : Response {
    val response = client.newCall(request).execute()
    return if (response.isSuccessful) response
    else {
        client.newCall(request).execute()
    }
}



