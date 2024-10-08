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

class FriendAdapter (
    private val friendList: List<User>,
    private val onAddToChatClick:(User) -> Unit
): RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendAdapter.FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_find_friend, parent, false)
        return FriendViewHolder(view)
    }
    override fun onBindViewHolder(holder: FriendAdapter.FriendViewHolder, position: Int) {
        val friend = friendList[position]

        holder.nameTextView.text = friend.userName
        holder.phoneTextView.text = friend.phoneNumber
        Glide.with(holder.itemView.context).load(friend.profileImage).into(holder.avatarImageView)

        holder.addFriendButton.setOnClickListener {
            onAddToChatClick
        }
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

    inner class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.friendName)
        val phoneTextView: TextView = view.findViewById(R.id.friendPhone)
        val avatarImageView: ImageView = view.findViewById(R.id.friendAvatar)
        val addFriendButton: Button = itemView.findViewById(R.id.btn_addFriend)
    }

}


