package com.example.myapplication.Activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.airbnb.lottie.LottieDrawable
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityVerificationBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class VerificationActivity : AppCompatActivity() {
    private lateinit var bind: ActivityVerificationBinding
    var auth: FirebaseAuth? = null
    var verificationId: String? = null
    var dialog: ProgressDialog? = null
    var otp: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
        initAnimation()
        inputOTP()

        dialog = ProgressDialog(this@VerificationActivity)
        dialog!!.setMessage("Đang xác thực...")
        dialog!!.setCancelable(false)
        dialog!!.show()

        auth = FirebaseAuth.getInstance()

        var phoneNumber = intent.getStringExtra("phone_number")
        bind.tvSdtVerify.text = phoneNumber

        if (!phoneNumber.isNullOrEmpty()) {

            // Xác thực số điện thoại tại đây
            val options = PhoneAuthOptions.newBuilder(auth!!)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this@VerificationActivity)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                        Toast.makeText(this@VerificationActivity, "Thành công!", Toast.LENGTH_SHORT).show()

                    }

                    override fun onVerificationFailed(p0: FirebaseException) {
                        Toast.makeText(this@VerificationActivity, "Xác thực số điện thoại thất bại", Toast.LENGTH_SHORT).show()
                        finish()
                    }

                    override fun onCodeSent(
                        verifyId: String,
                        forceResendingToken: PhoneAuthProvider.ForceResendingToken
                    ) {
                        super.onCodeSent(verifyId, forceResendingToken)
                        dialog!!.dismiss()
                        verificationId = verifyId
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                        bind.otp1.requestFocus()
                    }
                }).build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        } else {
            Toast.makeText(this@VerificationActivity, "Chưa nhận được số điện thoại", Toast.LENGTH_SHORT).show()
        }

        bind.otp6.addTextChangedListener(
            afterTextChanged = {
                if (bind.otp6.length() == 1) {
                    getOTP()
                    sendOTP()
                }

                if (bind.otp6.length() == 0) {
                    bind.otp5.requestFocus()
                }
            })
    }

    private fun sendOTP() {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp!!)
        auth!!.signInWithCredential(credential)
            .addOnCompleteListener {task ->
                if (task.isSuccessful){
                    val intent = Intent(this@VerificationActivity, SetUpProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@VerificationActivity, "Sai mã OTP", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun getOTP() {
        var l1 = bind.otp1.text.toString()
        var l2 = bind.otp2.text.toString()
        var l3 = bind.otp3.text.toString()
        var l4 = bind.otp4.text.toString()
        var l5 = bind.otp5.text.toString()
        var l6 = bind.otp6.text.toString()

        otp = l1 + l2 + l3 + l4 + l5 + l6

        Log.d("maOTPcheck", verificationId!!)
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
                if (bind.otp1.length() == 1) {
                    bind.otp2.requestFocus()
                }

            })

        bind.otp2.addTextChangedListener(
            afterTextChanged = {
                if (bind.otp2.length() == 1) {
                    bind.otp3.requestFocus()
                }

                if (bind.otp2.length() == 0) {
                    bind.otp1.requestFocus()
                }
            })

        bind.otp3.addTextChangedListener(
            afterTextChanged = {
                if (bind.otp3.length() == 1) {
                    bind.otp4.requestFocus()
                }

                if (bind.otp3.length() == 0) {
                    bind.otp2.requestFocus()
                }
            })


        bind.otp4.addTextChangedListener(
            afterTextChanged = {
                if (bind.otp4.length() == 1) {
                    bind.otp5.requestFocus()
                }

                if (bind.otp4.length() == 0) {
                    bind.otp3.requestFocus()
                }
            })

        bind.otp5.addTextChangedListener(
            afterTextChanged = {
                if (bind.otp5.length() == 1) {
                    bind.otp6.requestFocus()
                }

                if (bind.otp5.length() == 0) {
                    bind.otp4.requestFocus()
                }
            })

        bind.btnVerify.setOnClickListener {
            getOTP()
            sendOTP()
        }
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