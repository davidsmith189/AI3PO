package com.example.myapplication


import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Switch
import android.content.Intent

import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar= findViewById<androidx.appcompat.widget.Toolbar>(R.id.custom_toolbar)
        setSupportActionBar(toolbar)



        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        val saveIcon: ImageView = findViewById(R.id.save)

        fun saveChatWithMetadata(title: String, subject: String, timestamp: Long) {
            // Find HomeFragment and access its messages
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
                        ("timestamp" to it.timestamp ?: com.google.firebase.Timestamp.now()) as Pair<Any, Any>
                    )
                }
            )

            FirebaseFirestore.getInstance().collection("saved_chats")
                .add(chatMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Chat with metadata saved!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error saving chat", Toast.LENGTH_SHORT).show()
                }
        }


        fun showSaveMetadataDialog() {
            val dialogView = layoutInflater.inflate(R.layout.dialog_save_metadata, null)
            val editTitle = dialogView.findViewById<EditText>(R.id.editTitle)
            val editSubject = dialogView.findViewById<EditText>(R.id.editSubject)
            val timestampView = dialogView.findViewById<TextView>(R.id.timestampView)

            val timestamp = System.currentTimeMillis()
            timestampView.text = "Timestamp: $timestamp"

            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Save Chat Metadata")
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    val title = editTitle.text.toString()
                    val subject = editSubject.text.toString()

                    saveChatWithMetadata(title, subject, timestamp)
                }
                .setNegativeButton("Cancel", null)
            builder.show()
        }


        saveIcon.setOnClickListener {
            showSaveMetadataDialog()
        }






        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false  // Disable swipe gestures



        supportActionBar?.setDisplayShowTitleEnabled(false)



        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Discussion"
                    viewPager.setCurrentItem(0, false)
                }
                1 -> {
                    tab.text = "Doodle"
                    viewPager.setCurrentItem(1, false)
                }
                2 -> {
                    tab.text = "Saved"
                    viewPager.setCurrentItem(2, false)
                }
            }
        }.attach()


        val settingsButton = findViewById<ImageView>(R.id.settings)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }




    }

}
