package com.blueshark.lightcast.viewmodel

import androidx.databinding.ObservableBoolean
import com.blueshark.lightcast.listener.RequestDataReceiver
import com.blueshark.lightcast.model.ListData
import com.blueshark.lightcast.model.Release
import com.blueshark.lightcast.model.RequestParams
import com.blueshark.lightcast.networking.NewReleaseService
import com.google.gson.Gson
import java.io.Reader
import java.lang.Exception

class NewReleaseViewModel : BaseViewModel<ListData<Release>>(){

    val dataLoading = ObservableBoolean(false)

    init {
        netWorkService = NewReleaseService()
        refresh()
    }

    override fun loadData(offset: Int, size: Int, extras: Any?) {
        dataLoading.set(true)

        netWorkService.fetchData(RequestParams(offset, size), object : RequestDataReceiver{
            override fun onDataReceived(jsonString: String) {
                val gson = Gson()
                try {
                    val listData = gson.fromJson(jsonString, Array<Release>::class.java).toList()
                    mutableLiveData.postValue(ListData(offset, listData))
                } catch (e : Exception) {
                    mutableLiveData.postValue(null)
                }
                dataLoading.set(false)
            }
            override fun onDataReceivedStream(reader: Reader) {
                val gson = Gson()
                try {
                    val listData = gson.fromJson(reader, Array<Release>::class.java).toList()
                    mutableLiveData.postValue(ListData(offset, listData))
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

