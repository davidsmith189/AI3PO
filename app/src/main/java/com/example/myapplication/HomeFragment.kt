package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset
import kotlin.random.Random
import com.google.firebase.firestore.FieldValue

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var openAIService: OpenAIService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        openAIService = OpenAIService()

        chatAdapter = ChatAdapter(messages)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.chatRecyclerView.adapter = chatAdapter

        fetchMessages()

        binding.sendButton.setOnClickListener { sendMessage() }
        binding.btnAttach.setOnClickListener { showAttachmentMenu() }

        return binding.root
    }

    private fun showAttachmentMenu() {
        val popupMenu = PopupMenu(requireContext(), binding.btnAttach)
        popupMenu.menuInflater.inflate(R.menu.attachment_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_camera -> openCamera()
                R.id.menu_gallery -> openGallery()
                R.id.menu_document -> openDocuments()
            }
            true
        }
        popupMenu.show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun openDocuments() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        documentLauncher.launch(intent)
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            val message = "Image attached"
            val userMessage = ChatMessage(message, true)
            messages.add(userMessage)
            saveMessageToFirestore(userMessage)
            chatAdapter.notifyDataSetChanged()
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            val message = "Image attached"
            val userMessage = ChatMessage(message, true)

            messages.add(userMessage)
            saveMessageToFirestore(userMessage)
            chatAdapter.notifyDataSetChanged()
        }
    }

    private val documentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            val message = "Document attached"
            val userMessage = ChatMessage(message, true)

            messages.add(userMessage)
            saveMessageToFirestore(userMessage)
            chatAdapter.notifyDataSetChanged()
        }
    }

    private fun fetchMessages() {
        val db = FirebaseFirestore.getInstance()
        db.collection("openAIChats")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    messages.clear()
                    for (doc in snapshot.documents) {
                        val message = doc.getString("message") ?: ""
                        val isUser = doc.getBoolean("isUser") ?: false
                        messages.add(ChatMessage(message, isUser))
                    }
                    chatAdapter.notifyDataSetChanged()
                    binding.chatRecyclerView.scrollToPosition(messages.size - 1)
                }
            }
    }

    private fun saveMessageToFirestore(chatMessage: ChatMessage) {
        val db = FirebaseFirestore.getInstance()

        val data = hashMapOf(
            "message" to chatMessage.message,
            "isUser" to chatMessage.isUser,
            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        db.collection("openAIChats")
            .add(data)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "message saved with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "error saving message", e)
            }
    }

    private fun sendMessage() {
        val message = binding.userInput.text.toString().trim()
        if (message.isNotEmpty()) {
            val userMessage = ChatMessage(message, true)
            // Add user message
            messages.add(userMessage)

            binding.chatRecyclerView.post {
                chatAdapter.notifyDataSetChanged()
                binding.chatRecyclerView.scrollToPosition(messages.size - 1)
            }

            binding.userInput.text.clear()
            saveMessageToFirestore(userMessage)

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
                    val botMessage = ChatMessage(response,false)
                    messages.add(botMessage)
                    chatAdapter.notifyDataSetChanged()
                    binding.chatRecyclerView.scrollToPosition(messages.size - 1)
                    saveMessageToFirestore(botMessage)
                }
            }
        } else {
            println("Message was empty, not added.")
        }
    }


}
