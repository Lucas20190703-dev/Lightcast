package com.blueshark.lightcast

import android.app.Application
import com.blueshark.lightcast.utils.PreferenceUtil

class App : Application() {
    val preferencesUtility: PreferenceUtil
        get() = PreferenceUtil.getInstance(this@App)

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onTerminate() {
        super.onTerminate()
    }

    companion object {
        var instance: App? = null
    }
}