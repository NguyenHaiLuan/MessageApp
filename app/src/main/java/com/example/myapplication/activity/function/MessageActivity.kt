package com.example.myapplication.activity.function

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.adapter.MessageAdapter
import com.example.myapplication.databinding.ActivityMessageBinding
import com.example.myapplication.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import org.objectweb.asm.Handle
import java.util.Calendar
import java.util.Date
import java.util.logging.Handler

class MessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessageBinding
    private var messageAdapter: MessageAdapter? = null
    private var messages: ArrayList<Message>? = null
    private var senderRoom: String? = null
    private var receiveRoom: String? = null
    private var database: FirebaseDatabase? = null
    private var storage: FirebaseStorage? = null
    private var senderUid: String? = null
    private var receiverUid: String? = null
    private var dialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
        // Khởi tạo Firebase Database và Storage
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        senderUid = FirebaseAuth.getInstance().uid

        // Khởi tạo dialog
        initDialog()
        messages = ArrayList() // Tạo một danh sách trống để lưu tin nhắn

        // Lấy thông tin người nhận từ Intent được truyền khi mở activity này
        receiverUid = intent.getStringExtra("friendId")
        binding.userNameMessage.text = intent.getStringExtra("name")

        Glide.with(this@MessageActivity)
            .load(intent.getStringExtra("image"))
            .placeholder(R.drawable.user)
            .into(binding.avtUserMessage)

        // Thiết lập phòng chat giữa người gửi và người nhận
        senderRoom = senderUid + receiverUid
        receiveRoom = receiverUid + senderUid
        messageAdapter = MessageAdapter(this@MessageActivity, messages!!, senderRoom!!, receiveRoom!!)
        binding.recyclerViewMessage.layoutManager = LinearLayoutManager(this@MessageActivity)
        binding.recyclerViewMessage.adapter = messageAdapter

        // Lấy dữ liệu tin nhắn từ Firebase Realtime Database cho phòng chat của người gửi
        database!!.reference.child("chats")
            .child(senderRoom!!)
            .child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages!!.clear()
                    // Lặp qua các tin nhắn trong snapshot và thêm vào danh sách
                    for (snapshot1 in snapshot.children) {
                        val message: Message? = snapshot1.getValue(Message::class.java)
                        message!!.messageId = snapshot1.key
                        messages!!.add(message)
                    }
                    messageAdapter!!.notifyDataSetChanged()

                    // Cuộn xuống tin nhắn cuối cùng
                    binding.recyclerViewMessage.scrollToPosition(messages!!.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        binding.btnBack.setOnClickListener {
            finish()
        }

        // Xử lí sự kiện cho nút gửi tin nhắn
        binding.btnSendMessage.setOnClickListener {
            sendMessage()
        }

        binding.btnAddImage.setOnClickListener {
            uploadAttachment()
        }
    }


    override fun onPause() {
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25) {
            if (data != null) {
                val selectedImage = data.data
                val calendar = Calendar.getInstance()
                var reference = storage!!.reference.child("chats")
                    .child(calendar.timeInMillis.toString() + "")
                dialog!!.show()
                reference.putFile(selectedImage!!)
                    .addOnCompleteListener { task ->
                        dialog!!.dismiss()
                        if (task.isSuccessful) {
                            reference.downloadUrl.addOnSuccessListener { uri ->
                                val filePath = uri.toString()
                                val messageTxt = binding.edtMessage.text.toString()
                                val date = Date()
                                val message = Message(messageTxt, senderUid, date.time)
                                message.message = "photo"
                                message.imageMessageUrl = filePath
                                binding!!.edtMessage.setText("")
                                val randomKey = database!!.reference.push().key

                                val lastMessageObj = HashMap<String, Any>()
                                lastMessageObj["lastMessage"] = message.message!!
                                lastMessageObj["lastMessageTime"] = date.time

                                database!!.reference.child("chats").updateChildren(lastMessageObj)
                                database!!.reference.child("chats").child(receiveRoom!!)
                                    .updateChildren(lastMessageObj)
                                database!!.reference.child("chats").child(senderRoom!!)
                                    .child("messages").child(randomKey!!)
                                    .setValue(message).addOnSuccessListener {
                                        database!!.reference.child("chats")
                                            .child(senderRoom!!)
                                            .child("messages")
                                            .child(randomKey)
                                            .setValue(message)
                                            .addOnSuccessListener {
                                                database!!.reference.child("chats")
                                                    .child(receiveRoom!!)
                                                    .child("messages")
                                                    .child(randomKey!!)
                                                    .setValue(message).addOnSuccessListener{

                                                    }
                                            }
                                    }
                            }
                        }

                    }
            }
        }
    }

    // Hàm để upload ảnh đính kèm
    private fun uploadAttachment() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent, 25)
    }

    private fun sendMessage() {
        val messageText = binding.edtMessage.text.toString()
        val date = Date()
        val message = Message(messageText, senderUid, date.time)
        binding.edtMessage.setText("")

        val randomKey = database!!.reference.push().key

        // Cập nhật tin nhắn vào cả hai phòng chat
        val lastMessageObj = HashMap<String, Any>()
        lastMessageObj["lastMessage"] = message.message!!
        lastMessageObj["lastMessageTime"] = date.time

        database!!.reference.child("chats").child(senderRoom!!).updateChildren(lastMessageObj)
        database!!.reference.child("chats").child(receiveRoom!!).updateChildren(lastMessageObj)

        database!!.reference.child("chats").child(senderRoom!!).child("messages").child(randomKey!!)
            .setValue(message).addOnSuccessListener {
                database!!.reference.child("chats").child(receiveRoom!!).child("messages")
                    .child(randomKey).setValue(message)
            }
    }


    private fun initDialog() {
        dialog = ProgressDialog(this@MessageActivity)
        dialog!!.setMessage("Đang tải ảnh...")
        dialog!!.setCancelable(false)
    }

    private fun initUI() {
        enableEdgeToEdge()
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}