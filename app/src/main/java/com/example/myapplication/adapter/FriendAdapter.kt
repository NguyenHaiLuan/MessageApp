import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.model.User
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FriendAdapter(
    private val friendList: List<User>,
    private val onFriendSelected: (User) -> Unit
) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    private lateinit var conversationRef: DatabaseReference
    private lateinit var userId: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_find_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friendList[position]
        holder.userName.text = friend.userName

        // Sử dụng Glide/Picasso để load hình đại diện của bạn bè
        Glide.with(holder.itemView.context)
            .load(friend.profileImage)
            .into(holder.avatar)

        // Lấy userId từ FirebaseAuth
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Tham chiếu đến conversations của userId trong Firebase
        conversationRef = FirebaseDatabase.getInstance().getReference("conversations").child(userId)

        // Kiểm tra xem người bạn này đã có trong danh sách hội thoại chưa
        conversationRef.orderByChild("userName").equalTo(friend.userName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Nếu đã tồn tại người dùng trong danh sách conversation thì:
                    holder.addFriendButton.visibility = View.GONE
                    holder.checkDaThemView.visibility = View.VISIBLE
                } else {
                    holder.addFriendButton.visibility = View.VISIBLE
                    holder.checkDaThemView.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FriendAdapter", "Failed to check conversation: ${error.message}")
            }
        })

        // Xử lý sự kiện click vào nút thêm bạn bè
        holder.addFriendButton.setOnClickListener {
            onFriendSelected(friend)
        }
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.friendAvatar)
        val userName: TextView = itemView.findViewById(R.id.friendName)
        val addFriendButton: Button = itemView.findViewById(R.id.btn_addFriend)
        val checkDaThemView: ImageView = itemView.findViewById(R.id.daThemCheck)
    }
}
