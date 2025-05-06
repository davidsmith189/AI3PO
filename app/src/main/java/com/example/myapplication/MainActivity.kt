package com.example.myapplication


import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Switch
import android.content.Intent
import android.widget.Spinner

import android.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import android.net.Uri


class MainActivity : AppCompatActivity() {
    private var pendingDrawingUri: Uri? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar= findViewById<androidx.appcompat.widget.Toolbar>(R.id.custom_toolbar)
        setSupportActionBar(toolbar)



        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        val saveIcon: ImageView = findViewById(R.id.save)



        saveIcon.setOnClickListener {
            showSaveMetadataDialog()
        }






        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false  // Disable swipe gestures



        //supportActionBar?.setDisplayShowTitleEnabled(false)



        //TabLayoutMediator(tabLayout, viewPager) { tab, position ->
        //    when (position) {
        //        0 -> {
        //            tab.text = "Discussion"
        //            viewPager.setCurrentItem(0, false)
        //        }
        //        1 -> {
        //            tab.text = "Doodle"
        //            viewPager.setCurrentItem(1, false)
        //        }
        //        2 -> {
        //           tab.text = "Saved"
        //            viewPager.setCurrentItem(2, false)
        //        }
        //    }
        //}.attach()

        // set up the tabs
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Discussion"
                1 -> "Doodle"
                2 -> "Saved"
                else -> null
            }
        }.attach()

// **force** start on the Discussion tab
        viewPager.currentItem = 0


        val settingsButton = findViewById<ImageView>(R.id.settings)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

    }

    private fun saveChatWithMetadata(title: String, subject: String, timestamp: Long) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = currentUser.uid
        val homeFragment = supportFragmentManager.findFragmentByTag("f0") as? HomeFragment
        val chatMessages = homeFragment?.getChatMessages() ?: emptyList()

        val chatMap = hashMapOf(
            "title" to title,
            "subject" to subject,
            "timestamp" to timestamp,
            "messages" to chatMessages.map {
                hashMapOf(
                    "message" to it.message,
                    "isUser" to it.isUser,
                    "attachmentUri" to it.attachmentUri,
                    "timestamp" to (it.timestamp ?: com.google.firebase.Timestamp.now())
                )
            }
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("saved_chats")
            .add(chatMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Chat with metadata saved!", Toast.LENGTH_SHORT).show()

                // Reset chat session only after successfully saving the chat
                homeFragment?.resetChatSession()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving chat", Toast.LENGTH_SHORT).show()
            }
    }



    private fun showSaveMetadataDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_save_metadata, null)
        val editTitle = dialogView.findViewById<EditText>(R.id.editTitle)
        val spinnerSubject = dialogView.findViewById<Spinner>(R.id.spinnerSubject)
        val timestampView = dialogView.findViewById<TextView>(R.id.timestampView)

        val timestamp = System.currentTimeMillis()

        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy h:mm a", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("America/Los_Angeles")
        val formattedDate = sdf.format(java.util.Date(timestamp))

        timestampView.text = "Timestamp: $formattedDate"

        AlertDialog.Builder(this)
            .setTitle("Save Chat Metadata")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = editTitle.text.toString()
                var subject = spinnerSubject.selectedItem.toString()
                if (subject == "Select Subject..."){
                    subject = "Other"
                }
                saveChatWithMetadata(title, subject, timestamp)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun setPendingDrawingUri(uri: Uri) {
        pendingDrawingUri = uri
        
        // Get reference to the current HomeFragment and notify it about the new drawing
        val homeFragment = supportFragmentManager.fragments.firstOrNull { it is HomeFragment } as? HomeFragment
        homeFragment?.handleIncomingDrawing(uri)
    }
    
    fun getPendingDrawingUri(): Uri? {
        val uri = pendingDrawingUri
        pendingDrawingUri = null
        return uri
    }

}
