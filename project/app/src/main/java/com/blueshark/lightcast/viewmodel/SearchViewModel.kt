package com.blueshark.lightcast.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blueshark.lightcast.listener.RequestDataReceiver
import com.blueshark.lightcast.model.*
import com.blueshark.lightcast.networking.SearchService
import com.google.gson.Gson
import java.io.Reader
import java.lang.Exception

class SearchViewModel(private val query: String?) : BaseViewModel<ListData<SearchResultItem>>(){
    var totalResultLiveData : MutableLiveData<Int> = MutableLiveData()
    val dataLoading = ObservableBoolean(false)
    init {
        netWorkService = SearchService()
        loadData(0)
    }
    override fun loadData(offset: Int, size: Int, extras: Any?) {   //in this case offset is page number
        dataLoading.set(true)
        netWorkService.fetchData(SearchQueryParam(offset, query), object :
            RequestDataReceiver {
            override fun onDataReceived(jsonString: String) {
                val gson = Gson()
                try {
                    val podcastOverviewResponse =
                        gson.fromJson(jsonString, PodcastOverviewResponse::class.java)
                    totalResultLiveData.postValue(podcastOverviewResponse.totalResultCount)

                    val searchResultList = ArrayList<SearchResultItem>(0)
                    for (podcastOverview in podcastOverviewResponse.docSearchResults) {
                        val topPodcast = podcastOverview.topPodcast
                        topPodcast.episodes?.map {
                            SearchResultItem(
                                podcastId = topPodcast.podcastId,
                                name = topPodcast.name,
                                thumbnail = topPodcast.thumbnail,
                                description = topPodcast.description,
                                publishDate = topPodcast.publishDate,
                                releaseDate = topPodcast.releaseDate,
                                authorName = topPodcast.authorName,
                                isSubscribed = topPodcast.isSubscribed,
                                categories = topPodcast.categories,
                                episode = it,
                                artworkUrl600 = topPodcast.artworkUrl600
                            )
                        }?.let { searchResultList.addAll(it) }
                    }
                    mutableLiveData.postValue(ListData(offset, searchResultList))
                } catch (e : Exception) {
                    e.printStackTrace()
                    mutableLiveData.postValue(null)
                }
                dataLoading.set(false)
            }
            override fun onDataReceivedStream(reader: Reader) {
                val gson = Gson()
                try {
                    val podcastOverviewResponse =
                        gson.fromJson(reader, PodcastOverviewResponse::class.java)
                    totalResultLiveData.postValue(podcastOverviewResponse.totalResultCount)

                    val searchResultList = ArrayList<SearchResultItem>(0)
                    for (podcastOverview in podcastOverviewResponse.docSearchResults) {
                        val topPodcast = podcastOverview.topPodcast
                        topPodcast.episodes?.map {
                            SearchResultItem(
                                podcastId = topPodcast.podcastId,
                                name = topPodcast.name,
                                thumbnail = topPodcast.thumbnail,
                                description = topPodcast.description,
                                publishDate = topPodcast.publishDate,
                                releaseDate = topPodcast.releaseDate,
                                authorName = topPodcast.authorName,
                                isSubscribed = topPodcast.isSubscribed,
                                categories = topPodcast.categories,
                                episode = it,
                                artworkUrl600 = topPodcast.artworkUrl600
                            )
                        }?.let { searchResultList.addAll(it) }
                    }
                    mutableLiveData.postValue(ListData(offset, searchResultList))
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

    fun getTotalResultRepository(): LiveData<Int> {
        return totalResultLiveData
    }
}