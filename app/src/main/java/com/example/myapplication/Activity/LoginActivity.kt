package com.example.myapplication.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var bind: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
        logInButtonClick()
    }

    private fun logInButtonClick(){
        bind.btnLogIn.setOnClickListener {
            val phoneNumber = bind.edtSdtLogIn.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                val intent = Intent(this@LoginActivity, VerificationActivity::class.java)
                intent.putExtra("phone_number", phoneNumber)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initUI(){
        enableEdgeToEdge()
        bind = ActivityLoginBinding.inflate(layoutInflater)
        val view = bind.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}