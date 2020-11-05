package com.blueshark.lightcast.ui.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.blueshark.lightcast.viewmodel.BaseViewModel

abstract class BaseFragment<T>: Fragment(){

    lateinit var viewModel : BaseViewModel<T>

    @Suppress("UNCHECKED_CAST")
    protected inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
        object : ViewModelProvider.Factory {
            override fun <TT : ViewModel> create(aClass: Class<TT>):TT = f() as TT
        }


}