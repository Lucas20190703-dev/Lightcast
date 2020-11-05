package com.blueshark.lightcast.viewmodel

import androidx.databinding.ObservableBoolean
import com.blueshark.lightcast.listener.RequestDataReceiver
import com.blueshark.lightcast.model.*
import com.blueshark.lightcast.networking.PodcastOverviewService
import com.google.gson.Gson
import java.io.Reader
import java.lang.Exception

class PodcastOverviewViewModel(private var podcastId: Long) : BaseViewModel<SearchResult>() {

    val dataLoading = ObservableBoolean(false)

    init {
        netWorkService = PodcastOverviewService()
        refresh()
    }

    override fun loadData(offset: Int, size: Int, extras: Any?) {
        dataLoading.set(true)
        val realOffset = if (offset == 0) 0 else offset -1
        netWorkService.fetchData(PodcastRequestParams(realOffset , size, podcastId), object : RequestDataReceiver {
            override fun onDataReceived(jsonString: String) {
                val gson = Gson()
                try {
                    val response = gson.fromJson(jsonString, PodcastOverviewResponse::class.java)
                    if (response.docSearchResults.size > 0) {
                        mutableLiveData.postValue(
                            SearchResult(
                                response.docSearchResults[0],
                                response.episodeCount,
                                offset
                            )
                        )
                    } else {
                        mutableLiveData.postValue(SearchResult(null, response.episodeCount, offset))
                    }
                } catch (e : Exception) {
                    mutableLiveData.postValue(null)
                }
                dataLoading.set(false)
            }

            override fun onDataReceivedStream(reader: Reader) {
                val gson = Gson()
                try {
                    val response = gson.fromJson(reader, PodcastOverviewResponse::class.java)
                    if (response.docSearchResults.size > 0) {
                        mutableLiveData.postValue(
                            SearchResult(
                                response.docSearchResults[0],
                                response.episodeCount,
                                offset
                            )
                        )
                    } else {
                        mutableLiveData.postValue(SearchResult(null, response.episodeCount, offset))
                    }
                } catch (e : Exception) {
                    mutableLiveData.postValue(null)
                }
                dataLoading.set(false)
            }

            override fun onFailed(t: Throwable?) {
                mutableLiveData.postValue(null)
                dataLoading.set(false)
            }
        })
    }
}