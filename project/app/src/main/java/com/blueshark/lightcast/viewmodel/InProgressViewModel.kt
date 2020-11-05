package com.blueshark.lightcast.viewmodel

import androidx.databinding.ObservableBoolean
import com.blueshark.lightcast.listener.RequestDataReceiver
import com.blueshark.lightcast.model.InProgress
import com.blueshark.lightcast.model.InProgressResponse
import com.blueshark.lightcast.model.ListData
import com.blueshark.lightcast.model.RequestParams
import com.blueshark.lightcast.networking.InProgressService
import com.google.gson.Gson
import java.io.Reader
import java.lang.Exception

class InProgressViewModel : BaseViewModel<ListData<InProgress>>() {

    val dataLoading = ObservableBoolean(false)

    init {
        netWorkService = InProgressService()
        refresh()
    }

    override fun loadData(offset: Int, size: Int, extras: Any?) {

        dataLoading.set(true)
        netWorkService.fetchData(RequestParams(offset, size), object : RequestDataReceiver {
            override fun onDataReceived(jsonString: String) {
                val gson = Gson()
                try {
                    val inProgressResponse =
                        gson.fromJson(jsonString, InProgressResponse::class.java)
                    mutableLiveData.postValue(ListData(offset, inProgressResponse.inprogressDto))
                } catch (e : Exception){
                    mutableLiveData.postValue(null)
                }
                dataLoading.set(false)
            }

            override fun onDataReceivedStream(reader: Reader) {
                val gson = Gson()
                try {
                    val inProgressResponse = gson.fromJson(reader, InProgressResponse::class.java)
                    mutableLiveData.postValue(ListData(offset, inProgressResponse.inprogressDto))
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