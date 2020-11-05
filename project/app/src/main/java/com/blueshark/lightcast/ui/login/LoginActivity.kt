package com.blueshark.lightcast.ui.login

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.navigation.findNavController
import com.blueshark.lightcast.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //settingStatusBarTransparent(window)
        setContentView(R.layout.activity_login)
    }

    // back button support
    override fun onSupportNavigateUp() = this.findNavController(R.id.my_nav_host_login_fragment).navigateUp()

    fun showProgressbar(visible : Boolean) {
        when (visible) {
            true -> {
                loadingProgressbarLayout.visibility = View.VISIBLE
                loadingProgressbar.show()
            }
            else -> {
                loadingProgressbar.hide()
                loadingProgressbarLayout.visibility = View.GONE
            }
        }
    }
}