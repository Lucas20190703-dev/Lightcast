package com.blueshark.lightcast.ui.login

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.blueshark.lightcast.R
import com.blueshark.lightcast.model.User
import com.blueshark.lightcast.consts.*
import com.blueshark.lightcast.model.Address
import com.blueshark.lightcast.model.AuthenticationResult
import com.blueshark.lightcast.model.AuthenticationState
import com.blueshark.lightcast.ui.base.BaseFragment

import kotlinx.android.synthetic.main.fragment_login.*
import org.json.JSONObject

class LoginFragment : BaseFragment<AuthenticationResult>(){

    private lateinit var loginViewModel : LoginViewModel

    private var email : String? = null
    private var password : String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loginViewModel = ViewModelProvider(this, viewModelFactory {
            LoginViewModel()
        }).get(LoginViewModel::class.java)

        loginViewModel.getLoginRepository().observe(viewLifecycleOwner, Observer<AuthenticationResult?>{
            (activity as LoginActivity).showProgressbar(false);
            if (it!!.authenticationState == AuthenticationState.AUTHENTICATED) {
                activity?.finish()
                val startMainAction = LoginFragmentDirections.actionStartMain(email ?: "anonym")
                findNavController().navigate(startMainAction)
            } else if (it.authenticationState == AuthenticationState.INVALID_AUTHENTICATION){
                Toast.makeText(context, "Login Failed", Toast.LENGTH_LONG).show()
            }
        })
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as LoginActivity).showProgressbar(false)

        loginButton.setOnClickListener {
            email = loginEmailInput.text.toString()
            password = loginPasswordInput.text.toString()
            (activity as LoginActivity).showProgressbar(true)
            loginViewModel.authenticate(email, password)
        }

        loginPasswordInput.isLongClickable = false    //disable copy, past, cut
        registerButton.setOnClickListener{
            it.findNavController().navigate(R.id.action_login_to_register)
        }

        forgotPasswordTextView.setOnClickListener {
            it.findNavController().navigate(R.id.action_login_to_forgot)
        }

    }

    companion object {
        private const val TAG = "LoginFragment"

    }
}