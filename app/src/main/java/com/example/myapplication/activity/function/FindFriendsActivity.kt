package com.example.myapplication.activity.function

import FriendAdapter
import android.app.DownloadManager.Query
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityFindFriendsBinding
import com.example.myapplication.model.Conversation
import com.example.myapplication.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.github.muddz.styleabletoast.StyleableToast

class FindFriendsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFindFriendsBinding
    private lateinit var friendAdapter: FriendAdapter
    private lateinit var friendList: MutableList<User>
    private lateinit var database: DatabaseReference
    private lateinit var conversationRef: DatabaseReference
    private lateinit var userId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
        setupRecyclerView()

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        database = FirebaseDatabase.getInstance().getReference("users")
        conversationRef = FirebaseDatabase.getInstance().getReference("conversations").child(userId)

        loadAllUsers()

        binding.btnFindFriendFF.setOnClickListener {
            val query = binding.edtFindFriend.text.toString().trim()
            if (query.isEmpty()){
                StyleableToast.makeText(this@FindFriendsActivity, "Vui lòng điền thông tin bạn muốn tìm", R.style.warning)
                binding.edtFindFriend.requestFocus()
            } else {
                searchFriend(query)
            }
        }
    }

    private fun searchFriend(query: String) {
        if (query.matches("\\+\\d+".toRegex())) { // Kiểm tra nếu query bắt đầu bằng dấu "+" và có số
            handleQueryByPath("phoneNumber", query) // Tìm theo số điện thoại
        } else {
            handleAllUsers() // Tìm theo tên người dùng
        }
    }

    private fun handleQueryByPath(path: String, query: String) {
        val dataRef = FirebaseDatabase.getInstance().getReference("users")

        dataRef.orderByChild(path).equalTo(query) // Sử dụng equalTo() cho tìm kiếm số điện thoại
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    handleSearchResult(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    StyleableToast.makeText(this@FindFriendsActivity, "Đã có lỗi xảy ra!", R.style.error_toast).show()
                }
            })
    }


    private fun handleAllUsers() {
        val dataRef = FirebaseDatabase.getInstance().getReference("users")

        dataRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                handleFilteredResult(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                StyleableToast.makeText(this@FindFriendsActivity, "Đã có lỗi xảy ra!", R.style.error_toast).show()
            }
        })
    }

    private fun handleFilteredResult(snapshot: DataSnapshot) {
        val query = binding.edtFindFriend.text.toString().trim().lowercase() // Chuyển query thành chữ thường
        friendList.clear() // Xóa danh sách trước khi lọc

        for (data in snapshot.children) {
            val user = data.getValue(User::class.java)
            if (user != null) {
                val userNameLower = user.userName?.lowercase() // Chuyển tên người dùng thành chữ thường
                // Sử dụng contains để kiểm tra xem tên người dùng có chứa ký tự tìm kiếm không
                if (userNameLower != null && userNameLower.contains(query)) {
                    friendList.add(user) // Thêm vào danh sách nếu tên người dùng chứa ký tự tìm kiếm
                }
            }
        }
        // Cập nhật Adapter với danh sách bạn mới
        friendAdapter.notifyDataSetChanged()
    }

    private fun handleSearchResult(snapshot: DataSnapshot) {
        friendList.clear()
        for (data in snapshot.children) {
            val user = data.getValue(User::class.java)
            if (user != null) {
                friendList.add(user)
            }
        }

        // Cập nhật Adapter với danh sách bạn mới
        friendAdapter.notifyDataSetChanged()

        if (friendList.size==0) {
            binding.textKhongCoKetQua.visibility = View.VISIBLE
        } else{
            binding.textKhongCoKetQua.visibility = View.GONE
        }
    }

    private fun loadAllUsers() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                friendList.clear()
                for (friendSnapshot in snapshot.children) {
                    val friend = friendSnapshot.getValue(User::class.java)
                    friend?.let { friendList.add(it) }
                }
                friendAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FindFriendActivity", "Failed to load friends: ${error.message}")
            }
        })
    }

    private fun setupRecyclerView(){
        friendList = mutableListOf()
        friendAdapter = FriendAdapter(friendList, ::onFriendSelected)

        // Gán adapter cho RecyclerView
        binding.friendRecyclerView.adapter = friendAdapter
        binding.friendRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    // Hàm xử lý khi chọn một người bạn
    private fun onFriendSelected(friend: User) {
        // Tạo một đoạn hội thoại mới khi chọn bạn bè
        addFriendToConversations(friend)
    }

    // Thêm bạn bè vào danh sách cuộc hội thoại của người dùng hiện tại
    private fun addFriendToConversations(friend: User) {
        val newConversation = Conversation(
            userName = friend.userName.toString(),
            lastMessage = "Bạn chưa hỏi thăm người này!",
            timestamp = System.currentTimeMillis(),
            profileImage = friend.profileImage.toString().trim()
        )

        // Kiểm tra xem bạn đã được thêm vào conversation chưa
        conversationRef.orderByChild("userName").equalTo(friend.userName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    // Nếu bạn chưa tồn tại, thêm vào Firebase
                    conversationRef.push().setValue(newConversation).addOnSuccessListener {
                        StyleableToast.makeText(this@FindFriendsActivity, "Thêm thành công", R.style.success_toast).show()

                        // Chuyển sang MessageLobbyActivity sau khi thêm thành công
                        val intent = Intent(this@FindFriendsActivity, MessageLobbyActivity::class.java)
                        startActivity(intent)
                    }.addOnFailureListener {
                        StyleableToast.makeText(this@FindFriendsActivity, "Thêm thất bại!", R.style.error_toast).show()
                    }
                } else {
                    StyleableToast.makeText(this@FindFriendsActivity, "Người bạn này đã có trong danh sách bạn bè rồi!", R.style.warning).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FindFriendActivity", "Failed to check conversation: ${error.message}")
            }
        })
    }

    private fun initUI() {
        enableEdgeToEdge()
        binding = ActivityFindFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}