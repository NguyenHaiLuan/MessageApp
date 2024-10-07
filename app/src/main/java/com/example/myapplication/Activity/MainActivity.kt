package com.example.myapplication.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.myapplication.Model.User
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var auth: FirebaseAuth? = null
    private var database: FirebaseDatabase? = null
    private var storage: FirebaseStorage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()

        binding.progressBar.visibility = View.VISIBLE

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        val reference = database!!.getReference("users")

        reference.child(auth!!.currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Khi có dữ liệu, ẩn ProgressBar và cập nhật UI
                    binding.progressBar.visibility = View.GONE
                    binding.infomationBlock.visibility = View.VISIBLE
                    binding.funtionBlock.visibility = View.VISIBLE

                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        // Cập nhật UI với dữ liệu tên người dùng
                        binding.tvUserNameMain.text = user.userName
                        // Cập nhật UI với ảnh đại diện người dùng từ URL
                        val profileImageUrl = user.profileImage
                        if (!profileImageUrl.isNullOrEmpty()) {
                            // Sử dụng Glide để tải ảnh từ URL
                            Glide.with(this@MainActivity)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.user) // Ảnh mặc định khi đang tải
                                .into(binding.userAvtMain)

                            Log.d("link profileImageUrl", profileImageUrl)
                        } else {
                            Log.d("link profileImageUrl", "Profile image URL is empty")
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    binding.infomationBlock.visibility = View.VISIBLE
                    binding.funtionBlock.visibility = View.VISIBLE
                    Log.e("Firebase", "Error: ${databaseError.message}")
                }
            })


        binding.btnLogout.setOnClickListener {
            auth?.signOut()
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Xóa stack activity
            startActivity(intent)
            finish()
        }
    }

    private fun initUI() {
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}