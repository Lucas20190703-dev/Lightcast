package com.blueshark.lightcast.viewmodel

import androidx.databinding.ObservableBoolean
import com.blueshark.lightcast.listener.RequestDataReceiver
import com.blueshark.lightcast.model.ListData
import com.blueshark.lightcast.model.ListenLater
import com.blueshark.lightcast.model.ListenLaterResponse
import com.blueshark.lightcast.model.RequestParams
import com.blueshark.lightcast.networking.ListenLaterService
import com.google.gson.Gson
import java.io.Reader
import java.lang.Exception

class ListenLaterViewModel : BaseViewModel<ListData<ListenLater>>() {

    val dataLoading = ObservableBoolean(false)

    init {
        netWorkService = ListenLaterService()
        refresh()
    }

    override fun loadData(offset: Int, size: Int, extras: Any?) {
        dataLoading.set(true)
        netWorkService.fetchData(RequestParams(offset, size), object : RequestDataReceiver {
            override fun onDataReceived(jsonString: String) {
                val gson = Gson()
                try {
                    val listenLaterResponse =
                        gson.fromJson(jsonString, ListenLaterResponse::class.java)
                    mutableLiveData.postValue(ListData(offset, listenLaterResponse.bookMarks))
                } catch (e : Exception) {
                    e.printStackTrace()
                    mutableLiveData.postValue(null)
                }
                dataLoading.set(false)
            }
            override fun onDataReceivedStream(reader: Reader) {
                val gson = Gson()
                try {
                    val listenLaterResponse = gson.fromJson(reader, ListenLaterResponse::class.java)
                    mutableLiveData.postValue(ListData(offset, listenLaterResponse.bookMarks))
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