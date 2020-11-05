package com.blueshark.lightcast.viewmodel

import androidx.databinding.ObservableBoolean
import com.blueshark.lightcast.listener.RequestDataReceiver
import com.blueshark.lightcast.model.*
import com.blueshark.lightcast.networking.SearchSuggestService
import com.google.gson.Gson
import java.io.Reader
import java.lang.Exception

class SearchSuggestViewModel : BaseViewModel<ListData<PodcastOverview>>() {
    val dataLoading = ObservableBoolean(false)
    init {
        netWorkService = SearchSuggestService()
    }

    override fun loadData(offset: Int, size: Int, extras: Any?) {
        dataLoading.set(true)
        netWorkService.fetchData(SearchQueryParam(offset, (extras as String?)), object : RequestDataReceiver {
            override fun onDataReceived(jsonString: String) {
                val gson = Gson()
                try {
                    val searchResult =
                        gson.fromJson(jsonString, PodcastOverviewResponse::class.java)
                    mutableLiveData.postValue(ListData(offset, searchResult.docSearchResults))
                } catch (e : Exception) {
                    mutableLiveData.postValue(null)
                }
                dataLoading.set(false)
            }
            override fun onDataReceivedStream(reader: Reader) {

            }
            override fun onFailed(t: Throwable?) {
                mutableLiveData.postValue(null)
                dataLoading.set(false)
            }
        })
    }
}