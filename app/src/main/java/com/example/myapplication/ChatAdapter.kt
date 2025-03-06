package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemChatbotMessageBinding
import com.example.myapplication.databinding.ItemUserMessageBinding

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_BOT = 0
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_USER -> {
                val binding = ItemUserMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                UserMessageViewHolder(binding)
            }
            else -> {
                val binding = ItemChatbotMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BotMessageViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserMessageViewHolder -> {
                println("Binding User Message: ${message.message}")
                holder.bind(message)
            }
            is BotMessageViewHolder -> {
                println("Binding Bot Message: ${message.message}")
                holder.bind(message)
            }
        }
    }


    override fun getItemCount(): Int {
        println("Total messages in adapter: ${messages.size}")
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val type = if (messages[position].isUser) 1 else 0
        println("Message type at position $position: $type")
        return type
    }

}
