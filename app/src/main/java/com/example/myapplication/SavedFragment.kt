package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.util.Log
import java.io.IOException
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView



class SavedFragment: Fragment() {
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: SavedChatsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_saved, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)

        val jsonString = loadJsonFromAssets(requireContext(), "chats.json")
        if (jsonString != null) {
            val gson = Gson()
            val chatListType = object : TypeToken<List<SavedChat>>() {}.type
            val chats: List<SavedChat> = gson.fromJson(jsonString, chatListType)

            adapter = SavedChatsAdapter(chats)
            viewPager.adapter = adapter
        }
    }
}
fun loadJsonFromAssets(context: Context, filename: String): String? {
    return try {
        context.assets.open(filename).bufferedReader().use { it.readText() }
    } catch (ex: IOException) {
        ex.printStackTrace()
        null
    }
}

class SavedChatsAdapter(private val chatList: List<SavedChat>) :
    RecyclerView.Adapter<SavedChatsAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.chat_title)
        val messageText: TextView = itemView.findViewById(R.id.chat_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

        override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
            val chat = chatList[position]
            holder.titleText.text = chat.title
            holder.messageText.text = chat.lastMessage
            Log.d(
                "SavedChatsAdapter",
                "Binding position $position: title=${chat.title}, lastMessage=${chat.lastMessage}"
            )
        }

    override fun getItemCount(): Int = chatList.size

}


