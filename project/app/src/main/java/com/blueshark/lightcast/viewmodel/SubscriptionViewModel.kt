package com.blueshark.lightcast.viewmodel

import androidx.databinding.ObservableBoolean
import com.blueshark.lightcast.listener.RequestDataReceiver
import com.blueshark.lightcast.model.ListData
import com.blueshark.lightcast.model.Subscription
import com.blueshark.lightcast.model.SubscriptionResponse
import com.blueshark.lightcast.networking.SubscriptionService
import com.google.gson.Gson
import java.io.Reader
import java.lang.Exception

class SubscriptionViewModel : BaseViewModel<ListData<Subscription>>() {
    val dataLoading = ObservableBoolean(false)

    init {
        netWorkService = SubscriptionService()
        refresh()
    }

    override fun loadData(offset: Int, size: Int, extras: Any?) {
        dataLoading.set(true)
        netWorkService.fetchData(null, object : RequestDataReceiver {
            override fun onDataReceived(jsonString: String) {
                val gson = Gson()
                try {
                    val subscriptionResponse =
                        gson.fromJson(jsonString, SubscriptionResponse::class.java)
                    mutableLiveData.postValue(
                        ListData(
                            subscriptionResponse.subscribeDto.count(),
                            subscriptionResponse.subscribeDto
                        )
                    )
                } catch (e : Exception) {
                    mutableLiveData.postValue(null)
                }
                dataLoading.set(false)
            }
            override fun onDataReceivedStream(reader: Reader) {
                val gson = Gson()
                try {
                    val subscriptionResponse =
                        gson.fromJson(reader, SubscriptionResponse::class.java)
                    mutableLiveData.postValue(
                        ListData(
                            subscriptionResponse.subscribeDto.count(),
                            subscriptionResponse.subscribeDto
                        )
                    )
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