package com.example.myapplication

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ItemChatbotMessageBinding
import com.example.myapplication.databinding.ItemUserMessageBinding
import com.example.myapplication.databinding.ItemUserImageBinding
import com.example.myapplication.databinding.ItemUserDocumentBinding

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class TextMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val typingIndicator: TextView = itemView.findViewById(R.id.typingIndicator)
    }

    class ImageMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val imageView: ImageView = itemView.findViewById(R.id.attachmentImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = when (viewType) {
            VIEW_TYPE_USER_TEXT -> R.layout.item_user_message
            VIEW_TYPE_USER_IMAGE -> R.layout.item_user_image
            VIEW_TYPE_BOT_TEXT -> R.layout.item_bot_message
            VIEW_TYPE_BOT_IMAGE -> R.layout.item_bot_image
            else -> R.layout.item_bot_message
        }
        
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        
        return when (viewType) {
            VIEW_TYPE_USER_IMAGE, VIEW_TYPE_BOT_IMAGE -> ImageMessageViewHolder(view)
            else -> TextMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        
        when (holder) {
            is TextMessageViewHolder -> {
                if (message.isTyping) {
                    holder.messageText.visibility = View.GONE
                    holder.typingIndicator.visibility = View.VISIBLE
                } else {
                    holder.messageText.visibility = View.VISIBLE
                    holder.typingIndicator.visibility = View.GONE
                    holder.messageText.text = message.message
                }
            }
            is ImageMessageViewHolder -> {
                holder.messageText.text = message.message
                
                if (message.attachmentUri.isNotEmpty()) {
                    holder.imageView.visibility = View.VISIBLE
                    Glide.with(holder.imageView.context)
                        .load(Uri.parse(message.attachmentUri))
                        .into(holder.imageView)
                } else {
                    holder.imageView.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when {
            message.isUser && message.attachmentUri.isNotEmpty() -> VIEW_TYPE_USER_IMAGE
            message.isUser -> VIEW_TYPE_USER_TEXT
            !message.isUser && message.attachmentUri.isNotEmpty() -> VIEW_TYPE_BOT_IMAGE
            else -> VIEW_TYPE_BOT_TEXT
        }
    }

    companion object {
        private const val VIEW_TYPE_USER_TEXT = 1
        private const val VIEW_TYPE_BOT_TEXT = 2
        private const val VIEW_TYPE_USER_IMAGE = 3
        private const val VIEW_TYPE_BOT_IMAGE = 4
    }
}
