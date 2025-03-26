package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginScreen : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_screen)

        auth = FirebaseAuth.getInstance()

        val userOrEmail = findViewById<EditText>(R.id.usernameEditText)
        val password = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)

        // Suppose you have a button or some condition that triggers login success:
        // For illustration, we'll just start MainActivity immediately.
        loginButton.setOnClickListener {
            val userOrEmailString = userOrEmail.text.toString().trim()
            val passwordString = password.text.toString().trim()

            if(userOrEmailString.isNotEmpty() && passwordString.isNotEmpty()) {
                signInUser(userOrEmailString,passwordString)
            } else {
                Toast.makeText(this, "please enter both email and password", Toast.LENGTH_SHORT).show()
            }

        }


    }

    private fun signInUser(userOrEmailString: String, passwordString: String ) {
        auth.signInWithEmailAndPassword(userOrEmailString, passwordString)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.getIdToken(true)
                        ?.addOnCompleteListener { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                val idToken = tokenTask.result?.token
                                navigateToMainActivity()
                            } else {
                                Toast.makeText(this, "Token Generation Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createNewUser(userOrEmailString: String, passwordString: String) {
        auth.createUserWithEmailAndPassword(userOrEmailString, passwordString)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {

                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}