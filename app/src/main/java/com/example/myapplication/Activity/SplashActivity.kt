package com.example.myapplication.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    private lateinit var splashAnimation : LottieAnimationView
    private var auth : FirebaseAuth ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()

        // Xử lí animation và chuyển hướng sang activity tiếp theo
        splashAnimation = findViewById(R.id.splashAnimation)
        splashAnimation.playAnimation()
        splashAnimation.postDelayed({
            val intent = Intent(this@SplashActivity, SignUp::class.java)
            startActivity(intent)
            finish()
        }, 4000)
    }

    private fun initUI(){
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initAnimation(){

    }
}