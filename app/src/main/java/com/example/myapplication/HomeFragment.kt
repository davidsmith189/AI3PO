package com.example.myapplication

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var userInput: EditText
    private lateinit var sendButton: Button
    private lateinit var plusButton: ImageButton
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        chatRecyclerView = view.findViewById(R.id.chatRecyclerView)
        userInput = view.findViewById(R.id.userInput)
        sendButton = view.findViewById(R.id.sendButton)
        plusButton = view.findViewById(R.id.plusButton)

        // Set up RecyclerView
        chatAdapter = ChatAdapter(messages)
        chatRecyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true // Makes messages appear at the bottom
        }
        chatRecyclerView.adapter = chatAdapter

        sendButton.setOnClickListener { sendMessage() }

        return view
    }

    private fun sendMessage() {
        val message = userInput.text.toString().trim()
        if (message.isNotEmpty()) {
            messages.add(ChatMessage(message, true)) // Add user message
            chatAdapter.notifyItemInserted(messages.size - 1) // Notify adapter
            chatRecyclerView.scrollToPosition(messages.size - 1) // Auto-scroll
            userInput.text.clear()

            // Force RecyclerView to refresh
            chatAdapter.notifyDataSetChanged()

            // Debugging
            println("Messages List: $messages")
            // Simulate chatbot response after 1 second
            chatRecyclerView.postDelayed({ chatbotResponse() }, 1000)
        }
    }




    private fun chatbotResponse() {
        val response = "I'm just a bot 🤖! You said: '${messages.last().message}'"
        messages.add(ChatMessage(response, false)) // Add bot message
        chatAdapter.notifyItemInserted(messages.size - 1)
        chatRecyclerView.scrollToPosition(messages.size - 1) // Auto-scroll
    }
}
