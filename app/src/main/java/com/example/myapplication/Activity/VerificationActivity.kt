package com.example.myapplication.Activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.airbnb.lottie.LottieDrawable
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityVerificationBinding

class VerificationActivity : AppCompatActivity() {
    private lateinit var bind: ActivityVerificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
        initAnimation()
        inputOTP()

    }

    private fun initAnimation() {
        bind.gaAnimation.playAnimation()
        bind.gaAnimation.repeatCount = LottieDrawable.INFINITE
    }

    private fun inputOTP() {
        bind.otp1.hint = ""
        bind.otp1.requestFocus()

        bind.otp1.addTextChangedListener(
            afterTextChanged = {
                if (bind.otp1.length() == 1)
                {
                    bind.otp2.requestFocus()
                }

            })

        bind.otp2.addTextChangedListener(
            afterTextChanged = {
                if (bind.otp2.length() == 1){
                    bind.otp3.requestFocus()
                }

                if (bind.otp2.length() == 0){
                    bind.otp1.requestFocus()
                }
            })

        bind.otp3.addTextChangedListener(
            afterTextChanged = {
                if (bind.otp3.length() == 1){
                    bind.otp4.requestFocus()
                }

                if (bind.otp3.length() == 0){
                    bind.otp2.requestFocus()
                }
            })


        bind.otp4.addTextChangedListener(
            afterTextChanged = {
                if (bind.otp4.length() == 1){
                    bind.otp5.requestFocus()
                }

                if (bind.otp4.length() == 0){
                    bind.otp3.requestFocus()
                }
            })

        bind.otp5.addTextChangedListener(
            afterTextChanged = {
                if (bind.otp5.length() == 1){
                    bind.otp6.requestFocus()
                }

                if (bind.otp5.length() == 0){
                    bind.otp4.requestFocus()
                }
            })

        bind.otp6.addTextChangedListener(
            afterTextChanged = {
                if (bind.otp6.length() == 1){
                    sendOTPClick()
                }

                if (bind.otp6.length() == 0){
                    bind.otp5.requestFocus()
                }
            })

    }

    private fun sendOTPClick(){

    }


    private fun initUI() {
        enableEdgeToEdge()
        bind = ActivityVerificationBinding.inflate(layoutInflater)
        val view = bind.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}