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
            messages.add(ChatMessage(message, true))
            println("User message added: $message")

            binding.chatRecyclerView.post {
                chatAdapter.notifyDataSetChanged()
                binding.chatRecyclerView.scrollToPosition(messages.size - 1)
            }
            chatAdapter.notifyDataSetChanged()

            binding.userInput.text.clear()

            println("Current messages list: $messages")

            binding.chatRecyclerView.postDelayed({ chatbotResponse() }, 1000)
        } else {
            println("Message was empty, not added.")
        }
    }


    private fun chatbotResponse() {
        if (messages.isEmpty()) return

        val response = getRandomResponse()
        messages.add(ChatMessage(response, false))

        requireActivity().runOnUiThread {
            chatAdapter.notifyDataSetChanged()
            binding.chatRecyclerView.scrollToPosition(messages.size - 1)
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
