package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
        val registerButton = findViewById<TextView>(R.id.registerButton)




        loginButton.setOnClickListener {
            val userOrEmailString = userOrEmail.text.toString().trim()
            val passwordString = password.text.toString().trim()

            // DEV BYPASS: hardcoded username & password
            if (userOrEmailString == "dev" && passwordString == "1234") {
                Toast.makeText(this, "Dev mode activated", Toast.LENGTH_SHORT).show()
                UserData.username = "DevUser"
                navigateToMainActivity("DevUser")
                return@setOnClickListener
            }

            if(userOrEmailString.isNotEmpty() && passwordString.isNotEmpty()) {
                signInUser(userOrEmailString,passwordString)
            } else {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            }
        }

//        registerButton.setOnClickListener {
//            val userOrEmailString = userOrEmail.text.toString().trim()
//            val passwordString = password.text.toString().trim()
//
//            if (userOrEmailString.isNotEmpty() && passwordString.isNotEmpty()) {
//                createNewUser(userOrEmailString, passwordString)
//            } else {
//                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
//            }
//        }


    }

    private fun signInUser(userOrEmailString: String, passwordString: String ) {
        auth.signInWithEmailAndPassword(userOrEmailString, passwordString)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.getIdToken(true)
                        ?.addOnCompleteListener { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                UserData.username = userOrEmailString
                                navigateToMainActivity(userOrEmailString)
                            } else {
                                Toast.makeText(this, "Token Generation Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

//    private fun createNewUser(userOrEmailString: String, passwordString: String) {
//        auth.createUserWithEmailAndPassword(userOrEmailString, passwordString)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    UserData.username = "TestUser"
//                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
//                    navigateToMainActivity(userOrEmailString)
//                } else {
//
//                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//    }

    private fun navigateToMainActivity(userOrEmailString:String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("USER_OR_EMAIL",userOrEmailString) //Passes the username
        startActivity(intent)
        finish()
    }

    private fun navigateToSettingsActivity(userOrEmailString: String) {
        val intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra("USER_OR_EMAIL", userOrEmailString)  // Pass the string to SettingsActivity
        startActivity(intent)
    }


}