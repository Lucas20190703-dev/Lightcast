package com.blueshark.lightcast.viewmodel

import androidx.databinding.ObservableBoolean
import com.blueshark.lightcast.listener.RequestDataReceiver
import com.blueshark.lightcast.model.Episode
import com.blueshark.lightcast.model.EpisodeRequestParam
import com.blueshark.lightcast.networking.EpisodeOverviewService
import com.google.gson.Gson
import java.io.Reader
import java.lang.Exception

class EpisodeOverviewViewModel(private var podcastId: Long, private var episodeId: String) :
    BaseViewModel<Episode>() {

    val dataLoading = ObservableBoolean(false)

    init {
        netWorkService = EpisodeOverviewService()
        refresh()
    }

    override fun loadData(offset: Int, size: Int, extras: Any?) {
        dataLoading.set(true)
        netWorkService.fetchData(EpisodeRequestParam(podcastId, episodeId), object : RequestDataReceiver {
            override fun onDataReceived(jsonString: String) {
                val gson = Gson()
                try {
                    val episode = gson.fromJson(jsonString, Episode::class.java)
                    mutableLiveData.postValue(episode)
                } catch (e : Exception) {
                    mutableLiveData.postValue(null)
                }
                dataLoading.set(false)
            }

            override fun onDataReceivedStream(reader: Reader) {
                val gson = Gson()
                try {
                    val episode = gson.fromJson(reader, Episode::class.java)
                    mutableLiveData.postValue(episode)
                } catch (e : Exception) {
                    e.printStackTrace()
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