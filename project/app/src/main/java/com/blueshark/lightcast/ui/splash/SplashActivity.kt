package com.blueshark.lightcast.ui.splash

import android.app.ActivityOptions
import com.blueshark.lightcast.ui.login.LoginActivity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.blueshark.lightcast.MainActivity
import com.blueshark.lightcast.R
import com.blueshark.lightcast.utils.PreferenceUtil


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Make sure this is before calling super.onCreate
        //hideSystemUI(window)
        //setTheme(R.style.LauncherTheme)
        super.onCreate(savedInstanceState)
        scheduleSplashScreen()
    }

    private fun scheduleSplashScreen() {
        val splashScreenDuration = getSplashScreenDuration()
        Handler().postDelayed(
            {
                // After the splash screen duration, route to the right activities
                routeToAppropriatePage()
                finish()
            },
            splashScreenDuration
        )
    }

    private fun getSplashScreenDuration() = 200L

    private fun routeToAppropriatePage() {
        if (PreferenceUtil.getInstance().userAuthenticationState) {
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in, R.anim.slide_out)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent, options.toBundle())
        } else {
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in, R.anim.slide_out)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent, options.toBundle())
        }
    }
}