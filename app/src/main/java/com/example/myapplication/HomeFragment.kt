package com.example.myapplication

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentHomeBinding
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset
import kotlin.random.Random

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private val openAIService = OpenAIService()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        chatAdapter = ChatAdapter(messages)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.chatRecyclerView.adapter = chatAdapter

        binding.sendButton.setOnClickListener { sendMessage() }

        return binding.root
    }

    private fun sendMessage() {
        val message = binding.userInput.text.toString().trim()
        if (message.isNotEmpty()) {
            // Add user message
            messages.add(ChatMessage(message, true))
            
            binding.chatRecyclerView.post {
                chatAdapter.notifyDataSetChanged()
                binding.chatRecyclerView.scrollToPosition(messages.size - 1)
            }

            binding.userInput.text.clear()

            // Add typing indicator
            val typingMessage = ChatMessage("", false, true)
            messages.add(typingMessage)
            chatAdapter.notifyItemInserted(messages.size - 1)
            binding.chatRecyclerView.scrollToPosition(messages.size - 1)

            // Send message to OpenAI API
            openAIService.sendMessage(message) { response ->
                requireActivity().runOnUiThread {
                    // Remove typing indicator
                    messages.remove(typingMessage)
                    // Add bot response
                    messages.add(ChatMessage(response, false))
                    chatAdapter.notifyDataSetChanged()
                    binding.chatRecyclerView.scrollToPosition(messages.size - 1)
                }
            }
        } else {
            println("Message was empty, not added.")
        }
    }

    private fun getRandomResponse(): String {
        val jsonString = try {
            val inputStream = requireContext().assets.open("samples.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            return "Oops! No response found."
        }

        val jsonObject = JSONObject(jsonString)
        val responsesArray = jsonObject.getJSONArray("responses")
        val randomIndex = Random.nextInt(responsesArray.length())
        return responsesArray.getString(randomIndex)
    }
}
