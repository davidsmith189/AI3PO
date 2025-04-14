package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignupScreen : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_screen)

        auth = FirebaseAuth.getInstance()

        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val signUpButton = findViewById<Button>(R.id.signUpButton)

        signUpButton.setOnClickListener {
            val userOrEmailString = usernameEditText.text.toString().trim()
            val passwordString = passwordEditText.text.toString().trim()

            if (userOrEmailString.isNotEmpty() && passwordString.isNotEmpty()) {
                createNewUser(userOrEmailString, passwordString)
            } else {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createNewUser(userOrEmailString: String, passwordString: String) {
        auth.createUserWithEmailAndPassword(userOrEmailString, passwordString)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    UserData.username = "TestUser"
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity(userOrEmailString)
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMainActivity(userOrEmailString: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("USER_OR_EMAIL", userOrEmailString)
        startActivity(intent)
        finish()
    }
}
