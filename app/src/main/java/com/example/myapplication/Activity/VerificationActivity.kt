package com.example.myapplication.Activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.github.muddz.styleabletoast.StyleableToast
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
        dialog!!.setMessage("Đang xác minh số điện thoại...")
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
                        StyleableToast.makeText(
                            this@VerificationActivity,
                            "Thành công!",
                            R.style.success_toast
                        ).show()
                    }

                    override fun onVerificationFailed(p0: FirebaseException) {
                        StyleableToast.makeText(
                            this@VerificationActivity,
                            "Lỗi! Có lẽ số điện thoại bạn nhập chưa đúng định dạng!",
                            R.style.error_toast
                        ).show()
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
            StyleableToast.makeText(
                this@VerificationActivity,
                "Chưa nhận được số điện thoại",
                R.style.error_toast
            ).show()
        }

        //Xử lí khi nhập xong số 6
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
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth!!.currentUser!!.uid
                    // Kiểm tra xem người dùng đã có hồ sơ chưa
                    checkIfProfileExists(userId)
                } else {
                    StyleableToast.makeText(
                        this@VerificationActivity,
                        "Mã OTP chưa đúng! Vui lòng nhập lại!",
                        R.style.warning
                    ).show()
                }
            }
    }

    private fun checkIfProfileExists(userId: String) {
        // Tham chiếu đến nhánh "users" trong Firebase Database
        val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId)

        // Lấy dữ liệu của người dùng hiện tại
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Kiểm tra xem hồ sơ có tồn tại không
                if (dataSnapshot.exists()) {
                    // Nếu có hồ sơ, chuyển đến MainActivity
                    val intent = Intent(this@VerificationActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Nếu không có hồ sơ, chuyển đến SetUpProfileActivity
                    val intent = Intent(this@VerificationActivity, SetUpProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                StyleableToast.makeText(
                    this@VerificationActivity,
                    "Lỗi trong khi kiểm tra hồ sơ!",
                    R.style.error_toast
                ).show()
            }
        })
    }


    private fun getOTP() {
        var l1 = bind.otp1.text.toString().trim()
        var l2 = bind.otp2.text.toString().trim()
        var l3 = bind.otp3.text.toString().trim()
        var l4 = bind.otp4.text.toString().trim()
        var l5 = bind.otp5.text.toString().trim()
        var l6 = bind.otp6.text.toString().trim()

        otp = l1 + l2 + l3 + l4 + l5 + l6
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