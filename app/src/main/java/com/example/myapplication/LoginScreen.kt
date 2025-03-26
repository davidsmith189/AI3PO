package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LoginScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_screen)

        val loginButton = findViewById<Button>(R.id.loginButton)

        // Suppose you have a button or some condition that triggers login success:
        // For illustration, we'll just start MainActivity immediately.
        loginButton.setOnClickListener {
            navigateToMainActivity()
        }


    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // finish LoginScreen so that the user can't navigate back to it
    }
}