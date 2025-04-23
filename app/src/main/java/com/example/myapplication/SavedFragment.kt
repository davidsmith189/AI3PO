package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.ExpandableListView
import java.io.IOException
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class SavedFragment : Fragment() {
    private lateinit var expandableListView: ExpandableListView
    private lateinit var adapter: SavedChatsExpandableAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_saved, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expandableListView = view.findViewById(R.id.expandableListView)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: return Toast.makeText(requireContext(), "Not signed in", Toast.LENGTH_SHORT).show()

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("saved_chats")
            .get()
            .addOnSuccessListener { result ->
                val chats = result.map { doc ->
                    val title   = doc.getString("title")   ?: "Untitled"
                    val subject = doc.getString("subject") ?: "Other"
                    @Suppress("UNCHECKED_CAST")
                    val msgs = (doc.get("messages") as? List<HashMap<String,Any>>)
                        ?.map { m ->
                            ChatMessage(
                                message       = m["message"      ].toString(),
                                isUser        = m["isUser"       ] as Boolean,
                                attachmentUri = m["attachmentUri"]?.toString() ?: "",
                                timestamp     = m["timestamp"]   as? com.google.firebase.Timestamp
                            )
                        } ?: emptyList()
                    val lastMsg = msgs.lastOrNull()?.message ?: "No messages"
                    SavedChat(title, lastMsg, subject, msgs)
                }

                // group by subject
                val grouped = chats.groupBy { it.subject }
                val subjects = grouped.keys.toList()

                adapter = SavedChatsExpandableAdapter(requireContext(), subjects, grouped)
                expandableListView.setAdapter(adapter)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load saved chats", Toast.LENGTH_SHORT).show()
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

    private var onItemClickListener: ((SavedChat) -> Unit)? = null

    fun setOnItemClickListener(listener: (SavedChat) -> Unit) {
        onItemClickListener = listener
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.chat_title)
        val messageText: TextView = itemView.findViewById(R.id.chat_message)

        init {
            itemView.setOnClickListener {
                val savedChat = chatList[bindingAdapterPosition]
                val context = itemView.context
                val intent = Intent(context, ChatDetailActivity::class.java)
                // Pass the chat title
                intent.putExtra("chat_title", savedChat.title)
                // Convert the messages list to JSON using Gson (assuming savedChat.messages is a List<ChatMessage>)
                val messagesJson = Gson().toJson(savedChat.messages)
                intent.putExtra("chat_messages", messagesJson)
                context.startActivity(intent)
            }
        }
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


