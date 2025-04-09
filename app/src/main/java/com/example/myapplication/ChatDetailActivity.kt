package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChatDetailActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detail)

        // sets title of page to the chat_title used previously
        val chatTitle = intent.getStringExtra("chat_title") ?: "Chat Detail"
        // For example, if you have a TextView to display the title:
        findViewById<TextView>(R.id.chatTitleTextView).text = chatTitle

        // retrieve the JSON string for the chat messages from the intent extras.
        val chatMessagesJson = intent.getStringExtra("chat_messages")
        if (chatMessagesJson != null) {
            // parsing using gson for json
            val type = object : TypeToken<List<ChatMessage>>() {}.type
            val parsedMessages: List<ChatMessage> = Gson().fromJson(chatMessagesJson, type)
            messages.addAll(parsedMessages)
        }

        // resusing chatadapter for recyclerview
        val recyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(messages)
        recyclerView.adapter = chatAdapter

        fetchConversationFromFirestore()
        Log.d("ChatDetailActivity", "Number of messages: ${messages.size}")

    }

    private fun fetchConversationFromFirestore() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("ChatDetailActivity", "User not authenticated")
            return
        }
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(currentUser.uid)
            .collection("openAIChats")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { snapshot ->
                messages.clear()
                for (document in snapshot.documents) {
                    val messageText = document.getString("message") ?: ""
                    val isUser = document.getBoolean("isUser") ?: false
                    messages.add(ChatMessage(messageText, isUser))
                }
                Log.d("ChatDetailActivity", "Fetched ${messages.size} messages")
                chatAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("ChatDetailActivity", "Error fetching conversation", exception)
            }
    }
}
