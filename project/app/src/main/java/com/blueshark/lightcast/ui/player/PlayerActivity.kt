package com.blueshark.lightcast.ui.player

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blueshark.lightcast.R
import com.blueshark.lightcast.utils.settingStatusBarTransparent


class PlayerActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingStatusBarTransparent(window)
        setContentView(R.layout.activity_player)
    }
}