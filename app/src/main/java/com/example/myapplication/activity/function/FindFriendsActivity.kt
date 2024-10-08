package com.example.myapplication.activity.function

import FriendAdapter
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityFindFriendsBinding
import com.example.myapplication.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.github.muddz.styleabletoast.StyleableToast

class FindFriendsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFindFriendsBinding
    private lateinit var friendAdapter: FriendAdapter
    private val friendList = mutableListOf<User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
        setupRecyclerView()
        setupSearchListener()


    }

    private fun setupRecyclerView(){
        friendAdapter = FriendAdapter(friendList) {friend ->
            addUserToConversation(friend)
        }
        binding.friendRecyclerView.adapter = friendAdapter
        binding.friendRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupSearchListener(){
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

    private fun searchFriend(query:String){
        if (query.matches("\\d+".toRegex())){
            handleQueryByPath("phoneNumber", query)
        } else{
            handleQueryByPath("userName", query)
        }
    }

    private fun handleSearchResult(snapshot: DataSnapshot){
        friendList.clear()
        for (data in snapshot.children){
            val user = data.getValue(User::class.java)
            if (user!=null){
                friendList.add(user)
            }
        }
        // Cập nhật Adapter với danh sách bạn mới
        friendAdapter.notifyDataSetChanged()
    }

    private fun handleQueryByPath(path:String, query: String){
        val dataRef = FirebaseDatabase.getInstance().getReference("users")

        dataRef.orderByChild(path).startAt(query).endAt(query + "\uf8ff")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    handleSearchResult(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    StyleableToast.makeText(this@FindFriendsActivity, "Đã có lỗi xảy ra!", R.style.error_toast)
                }

            })
    }

    private fun addUserToConversation(friend:User){
        val databaseRef = FirebaseDatabase.getInstance().getReference("userConversation")
        val auth = FirebaseAuth.getInstance()
        val conversationId = databaseRef.push().key
        if (conversationId!=null){
            val conversationData = mapOf(
                "idUser" to auth!!.currentUser!!.uid,
                "idUserChatWith" to friend.userId
            )
            databaseRef.child(conversationId).setValue(conversationData).addOnCompleteListener {
                if (it.isSuccessful) {
                    StyleableToast.makeText(this@FindFriendsActivity, "Đã thêm vào danh sách tin nhắn", R.style.success_toast).show()
                    // Điều hướng tới màn hình chat với người đó nếu cần
                } else {
                    StyleableToast.makeText(this@FindFriendsActivity, "Lỗi khi thêm vào danh sách", R.style.error_toast).show()
                }
            }
        }
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


