package com.blueshark.lightcast.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blueshark.lightcast.networking.BaseNetWorkService

abstract class BaseViewModel<T> : ViewModel() {
    var mutableLiveData : MutableLiveData<T> = MutableLiveData()
    lateinit var netWorkService : BaseNetWorkService

    fun getNewRepository(): LiveData<T?>? {
        return mutableLiveData
    }

    fun refresh() {
        loadData(0)
    }

    abstract  fun loadData(offset : Int, size: Int = 10, extras : Any? = null)
}