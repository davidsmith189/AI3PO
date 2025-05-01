package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var chatAdapter: ChatAdapter
    private val messages: MutableList<ChatMessage> = mutableListOf()
    private lateinit var openAIService: OpenAIService
    private var currentPhotoUri: Uri? = null
    private var pendingImageUri: Uri? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

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

        openAIService = OpenAIService(requireContext()) { greeting ->
                requireActivity().runOnUiThread {
                    messages.add(ChatMessage(greeting, isUser = false))
                    chatAdapter.notifyItemInserted(messages.size - 1)
                }
        }

        chatAdapter = ChatAdapter(messages)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.chatRecyclerView.adapter = chatAdapter

        fetchMessages()

        binding.sendButton.setOnClickListener {
            sendMessage(pendingImageUri?.toString())
            pendingImageUri = null
        }

        binding.btnAttach.setOnClickListener {
            showAttachmentMenu()
        }
    }

    fun getChatMessages(): List<ChatMessage> {
        return messages
    }

    private fun showAttachmentMenu() {
        val popupMenu = PopupMenu(requireContext(), binding.btnAttach)
        popupMenu.menuInflater.inflate(R.menu.attachment_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_camera -> checkCameraPermission()
                R.id.menu_gallery -> openGallery()
                R.id.menu_document -> openDocuments()
                R.id.menu_clear_chat -> {
                    clearChat()
                    true
                }
            }
            true
        }
        popupMenu.show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private var isNewChatSession = false

    fun clearChat() {
        messages.clear()
        chatAdapter.notifyDataSetChanged()
        binding.chatRecyclerView.scrollToPosition(0)

        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        // Clear the temporary openAIChats collection
        db.collection("users")
            .document(currentUser.uid)
            .collection("openAIChats")
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    doc.reference.delete()
                }
            }

        isNewChatSession = true
    }


    fun resetChatSession() {
        isNewChatSession = false
        //fetchMessages() // Refresh messages after saving, if needed
    }


    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = requireContext().getExternalFilesDir("Images")
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(requireActivity().packageManager)?.let {
            try {
                val photoFile = createImageFile()
                currentPhotoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    photoFile
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
                cameraLauncher.launch(intent)
            } catch (ex: IOException) {
                Toast.makeText(requireContext(), "Error creating image file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun openDocuments() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        documentLauncher.launch(intent)
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                currentPhotoUri?.let { uri ->
                    // FileProvider URIs from our own app don't need persistable permissions
                    pendingImageUri = uri
                    binding.userInput.hint = "Ask about the image..."
                    Toast.makeText(requireContext(), "Image attached. Add a message and send.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    try {
                        // For content from other apps, we need to take persistable permissions
                        // but wrap it in a try-catch in case it fails
                        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        requireContext().contentResolver.takePersistableUriPermission(uri, takeFlags)
                    } catch (e: SecurityException) {
                        Log.d("HomeFragment", "Could not take persistable permission for URI $uri")
                    }
                    
                    pendingImageUri = uri
                    binding.userInput.hint = "Ask about the image..."
                    Toast.makeText(requireContext(), "Image attached. Add a message and send.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val documentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    try {
                        // For content from other apps, we need to take persistable permissions
                        // but wrap it in a try-catch in case it fails
                        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        requireContext().contentResolver.takePersistableUriPermission(uri, takeFlags)
                    } catch (e: SecurityException) {
                        Log.d("HomeFragment", "Could not take persistable permission for URI $uri")
                    }
                    
                    pendingImageUri = uri
                    binding.userInput.hint = "Ask about the document..."
                    Toast.makeText(requireContext(), "Document attached. Add a message and send.", Toast.LENGTH_SHORT).show()
                }
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
                if (snapshot != null && !isNewChatSession) {
                    messages.clear()
                    for (doc in snapshot.documents) {
                        val text = doc.getString("message") ?: ""
                        val isUser = doc.getBoolean("isUser") ?: false
                        val attachmentUri = doc.getString("attachmentUri") ?: ""
                        messages.add(ChatMessage(text, isUser, attachmentUri = attachmentUri))
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
            "isUser" to chatMessage.isUser,
            "timestamp" to FieldValue.serverTimestamp(),
            "attachmentUri" to chatMessage.attachmentUri
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

    private fun sendMessage(imageUri: String? = null) {
        val inputText = binding.userInput.text.toString().trim()
        if (inputText.isEmpty() && imageUri == null) {
            // Do nothing if blank and no image
            return
        }

        val userMessage = ChatMessage(inputText, true, attachmentUri = imageUri ?: "")
        messages.add(userMessage)
        chatAdapter.notifyDataSetChanged()
        binding.chatRecyclerView.scrollToPosition(messages.size - 1)
        binding.userInput.text.clear()
        binding.userInput.hint = "Type a message..."
        saveMessageToFirestore(userMessage)

        val typingIndicator = ChatMessage("", false, isTyping = true)
        messages.add(typingIndicator)
        chatAdapter.notifyItemInserted(messages.size - 1)
        binding.chatRecyclerView.scrollToPosition(messages.size - 1)

        openAIService.sendMessage(inputText, imageUri) { responseText ->
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

    fun handleIncomingDrawing(drawingUri: Uri) {
        // Set the drawing URI as the pending image URI
        pendingImageUri = drawingUri
        
        // Update the input hint to indicate a drawing is attached
        binding.userInput.hint = "Add context to your drawing..."
        
        // Show a toast message
        Toast.makeText(requireContext(), "Drawing attached. Add context and send.", Toast.LENGTH_SHORT).show()
    }
}
