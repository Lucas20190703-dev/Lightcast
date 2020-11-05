package com.blueshark.lightcast.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blueshark.lightcast.listener.RequestDataReceiver
import com.blueshark.lightcast.model.AuthenticationResult
import com.blueshark.lightcast.model.AuthenticationState
import com.blueshark.lightcast.model.LoginErrorCode
import com.blueshark.lightcast.model.UserResponse
import com.blueshark.lightcast.networking.LoginService
import com.blueshark.lightcast.utils.PreferenceUtil
import com.google.gson.Gson
import java.io.Reader
import java.lang.Exception


class LoginViewModel() : ViewModel() {
    private val mutableLiveData = MutableLiveData<AuthenticationResult>()
    private val loginService = LoginService()

    init {
        // In this example, the user is always unauthenticated when MainActivity is launched
        mutableLiveData.value = AuthenticationResult(AuthenticationState.UNAUTHENTICATED, null, null)
    }

    fun getLoginRepository() : LiveData<AuthenticationResult>{
        return mutableLiveData
    }

    fun authenticate(username: String?, password: String?){
        val checkResult = checkValidForInputData(username, password)
        if (username == null) {
            PreferenceUtil.getInstance().clearAll()
            mutableLiveData.postValue(AuthenticationResult(AuthenticationState.INVALID_AUTHENTICATION,
                LoginErrorCode.NULL_USER_NAME, "User name shouldn't be empty."))
        }
        if (password == null)
        {
            PreferenceUtil.getInstance().clearAll()
            mutableLiveData.postValue(AuthenticationResult(AuthenticationState.INVALID_AUTHENTICATION,
                LoginErrorCode.NULL_PASSWORD, "Please insert the password."))
        }

        loginService.login(username!!, password!!, object: RequestDataReceiver{
            override fun onDataReceived(jsonString: String) {
                val gson = Gson()
                try {
                    val userResponse = gson.fromJson(jsonString, UserResponse::class.java)
                    if (userResponse.errorCode != null && userResponse.message != null) {
                        PreferenceUtil.getInstance().clearAll()
                        mutableLiveData.postValue(AuthenticationResult(AuthenticationState.INVALID_AUTHENTICATION,
                            null, userResponse.message ))
                    } else if (userResponse.user != null) {
                        val accessToken = userResponse.accessToken
                        PreferenceUtil.getInstance().userInformation = userResponse.user
                        PreferenceUtil.getInstance().accessToken = accessToken
                        PreferenceUtil.getInstance().userAuthenticationState = true
                        mutableLiveData.postValue(AuthenticationResult(AuthenticationState.AUTHENTICATED,
                            null, null))
                    }
                } catch (e : Exception) {
                    e.printStackTrace()
                    mutableLiveData.postValue(AuthenticationResult(AuthenticationState.INVALID_AUTHENTICATION,
                        LoginErrorCode.NETWORK_FAILED, "Request failed"))
                }
            }

            override fun onDataReceivedStream(reader: Reader) {
                TODO("Not yet implemented")
            }

            override fun onFailed(t: Throwable?) {
                PreferenceUtil.getInstance().clearAll()
                mutableLiveData.postValue(AuthenticationResult(AuthenticationState.INVALID_AUTHENTICATION,
                    LoginErrorCode.NETWORK_FAILED, "Request failed"))
            }

        })
    }

    private fun checkValidForInputData(username: String?, password: String?): Boolean {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            //TODO

        }
        return true;
    }
}