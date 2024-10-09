package com.example.myapplication.activity.function

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.ConversationAdapter
import com.example.myapplication.databinding.ActivityLobbyMessageBinding
import com.example.myapplication.model.Conversation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MessageLobbyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLobbyMessageBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var conversationList: MutableList<Conversation>
    private lateinit var database: DatabaseReference
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()

        // nếu nhấn vào tiêu đề và nút back thì back về luôn
        binding.titleText.setOnClickListener {
            finish()
        }

        binding.backBtnLobby.setOnClickListener {
            finish()
        }

        // Khởi tạo danh sách cuộc hội thoại và adapter
        conversationList = mutableListOf()
        conversationAdapter = ConversationAdapter(conversationList)

        // Gán adapter cho RecyclerView
        recyclerView = binding.myFriendRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = conversationAdapter

        // Lấy userId từ FirebaseAuth
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Tham chiếu đến "conversations" trong Firebase Realtime Database
        database = FirebaseDatabase.getInstance().getReference("conversations").child(userId)

        // Load dữ liệu cuộc hội thoại từ Firebase
        loadConversations()
    }

    private fun initUI() {
        binding = ActivityLobbyMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    // Hàm load dữ liệu cuộc hội thoại từ Firebase
    private fun loadConversations() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                conversationList.clear()
                for (conversationSnapshot in snapshot.children) {
                    val conversation = conversationSnapshot.getValue(Conversation::class.java)
                    conversation?.let { conversationList.add(it) }
                }
                conversationAdapter.notifyDataSetChanged() // Cập nhật dữ liệu trong RecyclerView
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MessageLobbyActivity", "Failed to load conversations: ${error.message}")
            }
        })
    }
}
