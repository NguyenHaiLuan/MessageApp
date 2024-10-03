package com.example.myapplication.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivitySignUpBinding

class SignUp : AppCompatActivity() {
    private lateinit var bind: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
        backClick()
        signUpClick()

    }

    private fun backClick(){
        bind.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun signUpClick(){
        var phoneNumber : String = bind.edtSdtSignUp.text.toString()
        bind.btnSignUp.setOnClickListener {
            val intent = Intent(this, VerificationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initUI(){
        enableEdgeToEdge()
        bind = ActivitySignUpBinding.inflate(layoutInflater)
        val view = bind.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}