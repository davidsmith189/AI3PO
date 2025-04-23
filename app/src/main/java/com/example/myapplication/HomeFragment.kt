package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var chatAdapter: ChatAdapter
    private val messages: MutableList<ChatMessage> = mutableListOf()
    private lateinit var openAIService: OpenAIService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        openAIService = OpenAIService()

        chatAdapter = ChatAdapter(messages)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {

            stackFromEnd = true
        }
        binding.chatRecyclerView.adapter = chatAdapter

        fetchMessages()

        binding.sendButton.setOnClickListener {
            sendMessage()
        }

        binding.btnAttach.setOnClickListener {
            showAttachmentMenu()
        }
    }

    fun getChatMessages(): List<ChatMessage> {
        return messages
    }

    private fun showAttachmentMenu() {
        val popup = PopupMenu(requireContext(), binding.btnAttach)
        popup.menuInflater.inflate(R.menu.attachment_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_camera      -> {
                    openCamera()
                    true
                }
                R.id.menu_gallery     -> {
                    openGallery()
                    true
                }
                R.id.menu_document    -> {
                    openDocuments()
                    true
                }
                R.id.menu_clear_chat  -> {
                    clearChat()
                    true
                }
                else                  -> false
            }
        }
        popup.show()
    }

    fun clearChat() {
        messages.clear()
        chatAdapter.notifyDataSetChanged()
        // Scroll to top (empty list)
        binding.chatRecyclerView.scrollToPosition(0)
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

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // We ignore the actual Uri for now, just add a placeholder message
                val userMessage = ChatMessage("Image attached", true)
                messages.add(userMessage)
                chatAdapter.notifyDataSetChanged()
                saveMessageToFirestore(userMessage)
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val userMessage = ChatMessage("Image attached", true)
                messages.add(userMessage)
                chatAdapter.notifyDataSetChanged()
                saveMessageToFirestore(userMessage)
            }
        }

    private val documentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val userMessage = ChatMessage("Document attached", true)
                messages.add(userMessage)
                chatAdapter.notifyDataSetChanged()
                saveMessageToFirestore(userMessage)
            }
        }

    private fun fetchMessages() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val userId = currentUser.uid
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(userId)
            .collection("openAIChats")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("HomeFragment", "Firestore listen failed.", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    messages.clear()
                    for (doc in snapshot.documents) {
                        val text   = doc.getString("message") ?: ""
                        val isUser = doc.getBoolean("isUser") ?: false
                        messages.add(ChatMessage(text, isUser))
                    }
                    chatAdapter.notifyDataSetChanged()
                    binding.chatRecyclerView.scrollToPosition(messages.size - 1)
                }
            }
    }

    private fun saveMessageToFirestore(chatMessage: ChatMessage) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val userId = currentUser.uid
        val db = FirebaseFirestore.getInstance()

        val data = hashMapOf(
            "message" to chatMessage.message,
            "isUser"  to chatMessage.isUser,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("users")
            .document(userId)
            .collection("openAIChats")
            .add(data)
            .addOnSuccessListener { documentReference ->
                Log.d("HomeFragment", "Saved message with ID ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error saving message", e)
            }
    }

    private fun sendMessage() {
        val inputText = binding.userInput.text.toString().trim()
        if (inputText.isEmpty()) {
            // Do nothing if blank
            return
        }

        val userMessage = ChatMessage(inputText, true)
        messages.add(userMessage)
        chatAdapter.notifyDataSetChanged()
        binding.chatRecyclerView.scrollToPosition(messages.size - 1)
        binding.userInput.text.clear()
        saveMessageToFirestore(userMessage)

        val typingIndicator = ChatMessage("", false, isTyping = true)
        messages.add(typingIndicator)
        chatAdapter.notifyItemInserted(messages.size - 1)
        binding.chatRecyclerView.scrollToPosition(messages.size - 1)

        openAIService.sendMessage(inputText) { responseText ->
            requireActivity().runOnUiThread {

                messages.remove(typingIndicator)

                val botMessage = ChatMessage(responseText, false)
                messages.add(botMessage)
                chatAdapter.notifyDataSetChanged()
                binding.chatRecyclerView.scrollToPosition(messages.size - 1)
                saveMessageToFirestore(botMessage)
            }
        }
    }
}
