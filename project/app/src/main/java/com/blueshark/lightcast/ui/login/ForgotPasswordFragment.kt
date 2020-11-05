package com.blueshark.lightcast.ui.login

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_forgot_password.*

import com.blueshark.lightcast.R


class ForgotPasswordFragment : Fragment() {

    private enum class VerificationCodeState{
        CODE_SEND, CODE_TIME, CODE_RESEND
    }

    enum class TimerState{
        Stopped, Paused, Running
    }


    private var codeState : VerificationCodeState = VerificationCodeState.CODE_SEND
    private var timerState: TimerState = TimerState.Stopped
    private var secondsRemaining: Long = 60

    private lateinit var countDownTimer : CountDownTimer


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        forgotCodeStateText.setOnClickListener{
            onClickCodeSendButton()
        }
        updateRightDrawable()

        backImageButton.setOnClickListener {
            it.findNavController().popBackStack()
        }

        confirmButton.setOnClickListener{
            //....do something
            it.findNavController().popBackStack()
        }
    }


    private fun onClickCodeSendButton() {
        when(codeState) {
            VerificationCodeState.CODE_SEND, VerificationCodeState.CODE_RESEND -> {
                codeState = VerificationCodeState.CODE_TIME
                startTimer()
                sendRequestVerificationCode()
            }
            VerificationCodeState.CODE_TIME -> {
            }
        }
    }

    private fun sendRequestVerificationCode() {

    }

    private fun startTimer() {
        secondsRemaining = 10
        if (timerState == TimerState.Running) {
            countDownTimer.cancel()
        }
        timerState = TimerState.Running
        countDownTimer = object : CountDownTimer(secondsRemaining * 1000, 500) {
            override fun onFinish() {
                codeState = VerificationCodeState.CODE_RESEND
                updateRightDrawable()
            }

            override fun onTick(millisUntilFinished: Long) {
                val remaining = millisUntilFinished / 1000
                if (secondsRemaining != remaining) {
                    secondsRemaining = remaining
                    Log.e("ForgotPasswordFragment", "secondsRemaining = $secondsRemaining")
                    updateRightDrawable()
                }
            }
        }.start()
    }

    private fun updateRightDrawable() {
        when(codeState) {
            VerificationCodeState.CODE_SEND -> {
                forgotCodeStateText.text = getString(R.string.send)
                forgotCodeStateText.measure(0, 0)
                forgotCodeInput.setPadding(forgotCodeInput.paddingLeft, forgotCodeInput.paddingTop, forgotCodeStateText.measuredWidth, forgotCodeInput.paddingBottom)
            }
            VerificationCodeState.CODE_RESEND -> {
                forgotCodeStateText.text = getString(R.string.resend)
                forgotCodeStateText.measure(0, 0)
                forgotCodeInput.setPadding(forgotCodeInput.paddingLeft, forgotCodeInput.paddingTop, forgotCodeStateText.measuredWidth, forgotCodeInput.paddingBottom)
            }
            else -> {
                    forgotCodeStateText.text = "$secondsRemaining${getString(R.string.second)}"
                forgotCodeStateText.measure(0, 0)
                forgotCodeInput.setPadding(forgotCodeInput.paddingLeft, forgotCodeInput.paddingTop, forgotCodeStateText.measuredWidth, forgotCodeInput.paddingBottom)
            }
        }
    }

    companion object {

    }
}