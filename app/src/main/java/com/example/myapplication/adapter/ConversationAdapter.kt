package com.example.myapplication.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.activity.function.MessageActivity
import com.example.myapplication.model.Conversation
class ConversationAdapter(private val conversationList: List<Conversation>) : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conversation = conversationList[position]

        // Hiển thị thông tin của cuộc hội thoại
        holder.userName.text = conversation.userName
        holder.lastMessage.text = conversation.lastMessage
        holder.timeConversation.text = formatTime(conversation.timestamp)

        // Sử dụng thư viện như Glide/Picasso để load hình đại diện từ URL
        Glide.with(holder.itemView.context)
            .load(conversation.profileImage)
            .into(holder.avtConversation)

        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView.context, MessageActivity::class.java)
            intent.putExtra("name", conversation.userName)
            intent.putExtra("image", conversation.profileImage)
            intent.putExtra("friendId", conversation.userId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return conversationList.size
    }

    // Hàm chuyển đổi timestamp thành thời gian đọc được (ví dụ: 4m, 1h)
    private fun formatTime(timestamp: Long): String {
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - timestamp
        val minutes = diff / (1000 * 60)
        return if (minutes < 60) {
            "${minutes}m"
        } else {
            val hours = minutes / 60
            "${hours}h"
        }
    }

    inner class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avtConversation: ImageView = itemView.findViewById(R.id.avtConversation)
        val userName: TextView = itemView.findViewById(R.id.userNameConversation)
        val lastMessage: TextView = itemView.findViewById(R.id.lastMessage)
        val timeConversation: TextView = itemView.findViewById(R.id.timeConversation)
    }
}
