package com.example.myapplication.Activity

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.Model.User
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivitySetUpProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import io.github.muddz.styleabletoast.StyleableToast
import java.util.Date

class SetUpProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetUpProfileBinding
    private var auth :FirebaseAuth? =null
    private var database:FirebaseDatabase?=null
    private var storage : FirebaseStorage?=null
    private var selectedImage:Uri?=null
    private var dialog:ProgressDialog?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
        initDialog()

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        binding!!.avtSetUp.setOnClickListener{
            pickImage()
        }

        binding!!.btnChonAnh.setOnClickListener {
            pickImage()
        }

        binding.btnApplyProfile.setOnClickListener {
            val name = binding.edtNameProfile.text.toString()
            if (name.isEmpty()){
                StyleableToast.makeText(this@SetUpProfileActivity, "Bạn chưa nhập tên", R.style.warning).show()
                dialog!!.dismiss()
                binding.edtNameProfile.requestFocus()
            }
            dialog!!.show()
            if (selectedImage != null){
                val reference = storage!!.reference.child("Profile").child(auth!!.uid!!)
                reference.putFile(selectedImage!!).addOnCompleteListener {task->
                    if (task.isSuccessful){
                        reference.downloadUrl.addOnCompleteListener {uri ->
                            val profileImage = uri.toString()
                            val userId = auth!!.uid
                            val userName = binding.edtNameProfile.text.toString()
                            val phoneNumber = auth!!.currentUser!!.phoneNumber

                            val user = User(userId, userName, phoneNumber, profileImage)
                            database!!.reference
                                .child("users")
                                .child(userId!!)
                                .setValue(user)
                                .addOnCompleteListener{
                                    dialog!!.dismiss()
                                    val intent = Intent(this@SetUpProfileActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                        }
                    } else {
                        StyleableToast.makeText(this@SetUpProfileActivity, "Bạn chưa chọn ảnh!", R.style.warning).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data!=null)
            if (data.data != null){
                val uri = data.data
                val storage = FirebaseStorage.getInstance()
                val time = Date().time

                val reference = storage.reference
                    .child("Profile")
                    .child(time.toString() + "")
                reference.putFile(uri!!).addOnCompleteListener{task->
                    if (task.isSuccessful){
                        reference.downloadUrl.addOnCompleteListener {uri ->
                            val filePath = uri.toString()
                            val obj = HashMap<String, Any>()
                            obj["image"] = filePath
                            database!!.reference
                                .child("users")
                                .child(FirebaseAuth.getInstance().uid!!)
                                .updateChildren(obj).addOnSuccessListener {}
                        }
                    }
                }

                binding.avtSetUp.setImageURI(data.data)
                selectedImage = data.data
            }
    }

    private fun pickImage(){
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent,45)

    }

    private fun initDialog(){
        dialog = ProgressDialog(this@SetUpProfileActivity)
        dialog!!.setMessage("Đang cập nhật thông tin của bạn...")
        dialog!!.setCancelable(false)
    }

    private fun initUI(){
        enableEdgeToEdge()
        binding = ActivitySetUpProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}