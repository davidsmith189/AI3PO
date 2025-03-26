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
            messages.add(ChatMessage(attachmentUri = uri.toString(), isUser = true))
            saveMessageToFirestore(ChatMessage(attachmentUri = uri.toString(), isUser = true))
            chatAdapter.notifyDataSetChanged()
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            messages.add(ChatMessage(attachmentUri = uri.toString(), isUser = true))
            saveMessageToFirestore(ChatMessage(attachmentUri = uri.toString(), isUser = true))
            chatAdapter.notifyDataSetChanged()
        }
    }

    private val documentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            messages.add(ChatMessage(attachmentUri = uri.toString(), isUser = true))
            saveMessageToFirestore(ChatMessage(attachmentUri = uri.toString(), isUser = true))
            chatAdapter.notifyDataSetChanged()
        }
    }

    private fun saveMessageToFirestore(chatMessage: ChatMessage) {
        val db = FirebaseFirestore.getInstance()

        val data = hashMapOf(
            "message" to chatMessage.message,
            "isUser" to chatMessage.isUser,
            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
            "attachmentUri" to chatMessage.attachmentUri
          )

        db.collection("chats")
            .add(data)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "message saved with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "error savind message", e)
            }
    }

    private fun sendMessage() {
        val message = binding.userInput.text.toString().trim()
        if (message.isNotEmpty()) {

            val messageText = message
            val chatMessage = ChatMessage(messageText, isUser = true)

            messages.add(ChatMessage(message, true))

            saveMessageToFirestore(chatMessage)

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
