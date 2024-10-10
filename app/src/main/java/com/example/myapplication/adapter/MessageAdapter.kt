package com.example.myapplication.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.DeleteLayoutBinding
import com.example.myapplication.databinding.ItemReceiveMessageBinding
import com.example.myapplication.databinding.ItemSendMessageBinding
import com.example.myapplication.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MessageAdapter (
    var context: Context,
    messages:ArrayList<Message>,
    senderRoom:String,
    receiverRoom:String
): RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    lateinit var message: ArrayList<Message>
    val ITEM_SEND = 1
    val ITEM_RECEIVE = 2
    val senderRoom:String
    var receiverRoom:String

    inner class SendMessageViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        var binding:ItemSendMessageBinding = ItemSendMessageBinding.bind(itemView)
    }

    inner class ReceiveMessageViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        var binding:ItemReceiveMessageBinding = ItemReceiveMessageBinding.bind(itemView)
    }

    init {
        if (messages!=null){
            this.message = messages
        }
        this.senderRoom = senderRoom
        this.receiverRoom = receiverRoom

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType==ITEM_SEND){
            val view = LayoutInflater.from(context).inflate(R.layout.item_send_message, parent, false)
            SendMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_receive_message, parent, false)
            ReceiveMessageViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = message[position]

        return if (FirebaseAuth.getInstance().uid == message.senderId){
            ITEM_SEND
        } else{
            ITEM_RECEIVE
        }
    }
    override fun getItemCount(): Int {
        return message.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = message[position]
        if (holder.javaClass == SendMessageViewHolder::class.java){
            val viewHolder = holder as SendMessageViewHolder
            if (message.message.equals("photo")){
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.messageBlock.visibility = View.GONE
                Glide.with(context).load(message.imageMessageUrl).placeholder(R.drawable.place_holder_image).into(viewHolder.binding.image)
            }
            viewHolder.binding.messageND.text = message.message
            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding:DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Bạn có chắc muốn xoá?")
                    .setView(binding.root)
                    .create()

                binding.deleteForAll.setOnClickListener {
                    message.message = "Tin nhắn đã được thu hồi!"
                    message.messageId?.let {it1->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(it1)
                            .setValue(message)
                    }

                    message.messageId?.let {it1->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .child(it1!!)
                            .setValue(message)
                    }
                    dialog.dismiss()
                }
                binding.deleteOnlyMe.setOnClickListener {
                    message.messageId?.let {it1->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(it1)
                            .setValue(null)
                    }
                    dialog.dismiss()
                }
                binding.cancelDelete.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
                false
            }
        }
        else {
            val viewHolder = holder as ReceiveMessageViewHolder

            if (message.message == "photo") {
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.messageBlock.visibility = View.GONE
                viewHolder.binding.messageND.visibility = View.GONE
                Glide.with(context).load(message.imageMessageUrl)
                    .placeholder(R.drawable.place_holder_image).into(viewHolder.binding.image)
            }

            viewHolder.binding.messageND.text = message.message
            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Bạn có chắc muốn xoá?")
                    .setView(binding.root)
                    .create()

                binding.deleteForAll.setOnClickListener {
                    message.message = "Tin nhắn đã được thu hồi!"
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(it1)
                            .setValue(message)
                    }

                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .child(it1!!)
                            .setValue(message)
                    }
                    dialog.dismiss()
                }
                binding.deleteOnlyMe.setOnClickListener {
                    message.message = "Tin nhắn đã được thu hồi!"
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(it1)
                            .setValue(null)
                    }

                    dialog.dismiss()
                }
                binding.cancelDelete.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
                false
            }
        }
    }
}