package com.example.myapplication

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemChatbotMessageBinding
import com.example.myapplication.databinding.ItemUserMessageBinding
import com.example.myapplication.databinding.ItemUserImageBinding
import com.example.myapplication.databinding.ItemUserDocumentBinding

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_BOT = 0
        private const val TYPE_USER_IMAGE = 2
        private const val TYPE_USER_DOCUMENT = 3
    }

    class UserMessageViewHolder(private val binding: ItemUserMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.messageText.text = message.message
        }
    }

    class BotMessageViewHolder(private val binding: ItemChatbotMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.messageText.text = message.message
        }
    }

    class UserImageViewHolder(private val binding: ItemUserImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.imageView.setImageURI(Uri.parse(message.attachmentUri))
        }
    }

    class UserDocumentViewHolder(private val binding: ItemUserDocumentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.documentName.text = "Attached File"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_USER -> {
                val binding = ItemUserMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                UserMessageViewHolder(binding)
            }
            TYPE_BOT -> {
                val binding = ItemChatbotMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BotMessageViewHolder(binding)
            }
            TYPE_USER_IMAGE -> {
                val binding = ItemUserImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                UserImageViewHolder(binding)
            }
            TYPE_USER_DOCUMENT -> {
                val binding = ItemUserDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                UserDocumentViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid ViewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserMessageViewHolder -> holder.bind(message)
            is BotMessageViewHolder -> holder.bind(message)
            is UserImageViewHolder -> holder.bind(message)
            is UserDocumentViewHolder -> holder.bind(message)
        }
        //when (holder) {
        //    is UserMessageViewHolder -> {
        //        println("Binding User Message: ${message.message}")
        //        holder.bind(message)
        //    }
        //    is BotMessageViewHolder -> {
        //        println("Binding Bot Message: ${message.message}")
        //    holder.bind(message)
        //    }
        //}
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when {
            message.attachmentUri?.endsWith(".jpg", true) == true ||
                    message.attachmentUri?.endsWith(".png", true) == true -> TYPE_USER_IMAGE
            message.attachmentUri != null -> TYPE_USER_DOCUMENT
            message.isUser -> TYPE_USER
            else -> TYPE_BOT
        }
    }


    /* override fun getItemCount(): Int {
        println("Total messages in adapter: ${messages.size}")
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val type = if (messages[position].isUser) 1 else 0
        println("Message type at position $position: $type")
        return type
    }*/

}
