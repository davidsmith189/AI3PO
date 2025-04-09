package com.example.myapplication


import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar= findViewById<androidx.appcompat.widget.Toolbar>(R.id.custom_toolbar)
        setSupportActionBar(toolbar)

        val username=intent.getStringExtra("USERNAME")?: "User Name"
        val descriptionText = findViewById<TextView>(R.id.descriptionText)
        descriptionText.text = username  // Set the retrieved username

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        val saveIcon: ImageView = findViewById(R.id.save)

        saveIcon.setOnClickListener {
            showSaveMetadataDialog()
        }

        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        supportActionBar?.setDisplayShowTitleEnabled(false)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Discussion"
                1 -> tab.text = "Doodle"
                2 -> tab.text = "Saved"
            }
        }.attach()
    }

    private fun saveChatWithMetadata(title: String, subject: String, timestamp: Long) {
        val homeFragment = supportFragmentManager.findFragmentByTag("f0") as? HomeFragment
        val chatMessages = homeFragment?.getChatMessages() ?: emptyList()

        val messagesData = chatMessages.map {
            hashMapOf(
                "message" to it.message,
                "isUser" to it.isUser,
                "attachmentUri" to it.attachmentUri,
                "timestamp" to (it.timestamp ?: Timestamp.now())
            )
        }

        val chatMap = hashMapOf(
            "title" to title,
            "subject" to subject,
            "timestamp" to Timestamp(timestamp / 1000, ((timestamp % 1000) * 1000000).toInt()),
            "messages" to messagesData
        )

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("saved_chats")
            .add(chatMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Chat with metadata saved!", Toast.LENGTH_SHORT).show()
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
        timestampView.text = "Timestamp: $timestamp"

        AlertDialog.Builder(this)
            .setTitle("Save Chat Metadata")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = editTitle.text.toString()
                val subject = spinnerSubject.selectedItem.toString()
                saveChatWithMetadata(title, subject, timestamp)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
