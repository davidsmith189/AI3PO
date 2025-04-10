package com.example.myapplication
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar


class SettingsActivity : AppCompatActivity() {

    private lateinit var darkModeSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_page)

        // Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.settingsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        //val userOrEmailString = intent.getStringExtra("USER_OR_EMAIL")
        //val usernameTextView = findViewById<TextView>(R.id.usernameText)  // Example TextView
        //usernameTextView.text = userOrEmailString  // Display the string in a TextView


        val username = UserData.username ?: "Default User"
        val usernameTextView = findViewById<TextView>(R.id.usernameText)
        usernameTextView.text = username


        //val username=intent.getStringExtra("USERNAME")?: "User Name"
        //val descriptionText = findViewById<TextView>(R.id.usernameText)
        //descriptionText.text = username  // Set the retrieved username

        // Handle back arrow
        toolbar.setNavigationOnClickListener {
            finish()
        }



        // Load theme based on saved preferences
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Setup dark mode switch
        darkModeSwitch = findViewById(R.id.darkModeSwitch)
        darkModeSwitch.isChecked = isDarkMode

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            val editor = prefs.edit()
            editor.putBoolean("dark_mode", isChecked)
            editor.apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }


        val logoutButton = findViewById<Button>(R.id.logoutButton)

        logoutButton.setOnClickListener {
            // Sign out the user from Firebase
            FirebaseAuth.getInstance().signOut()

            // Start Login
            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)


            finish()
        }

    }




}