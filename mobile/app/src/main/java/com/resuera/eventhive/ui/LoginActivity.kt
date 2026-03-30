package com.resuera.eventhive.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.resuera.eventhive.R
import com.resuera.eventhive.api.RetrofitClient
import com.resuera.eventhive.model.AuthResponse
import com.resuera.eventhive.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailField = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordField = findViewById<TextInputEditText>(R.id.passwordInput)
        val loginBtn = findViewById<Button>(R.id.loginButton)

        loginBtn.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create the User object to send to Spring Boot
            // We pass null for names and ID because the Login endpoint only needs credentials
            val loginRequest = User(
                userID = null,
                userEmail = email,
                userPassword = password,
                userFirstName = null,
                userLastName = null
            )

            performLogin(loginRequest)
        }

        val btnRegister = findViewById<TextView>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

            finish()
        }
    }

    private fun performLogin(userRequest: User) {
        // 1. Call the singleton instance of Retrofit
        RetrofitClient.instance.login(userRequest).enqueue(object : Callback<AuthResponse> {

            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    val auth = response.body()

                    if (auth != null) {
                        // SUCCESS: Save User ID to SharedPreferences for future requests
                        saveUserSession(auth.id)

                        Toast.makeText(this@LoginActivity, "Welcome, ${auth.firstname}!", Toast.LENGTH_SHORT).show()

                        // Move to the Dashboard
                        val intent = Intent(this@LoginActivity, DashboardActivity::class.java)

                        intent.putExtra("USER_FIRSTNAME", auth.firstname)
                        intent.putExtra("USER_LASTNAME", auth.lastname)
                        intent.putExtra("USER_EMAIL", auth.email)

                        startActivity(intent)
                        finish() // Close LoginActivity so user can't "back" into it
                    }
                } else {
                    // SERVER ERROR (e.g., 401 Unauthorized or 404 Not Found)
                    Toast.makeText(this@LoginActivity, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                // NETWORK ERROR (e.g., Spring Boot server is down or No Internet)
                Toast.makeText(this@LoginActivity, "Connection Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun saveUserSession(userId: Long?) {
        val sharedPref = getSharedPreferences("EventHivePrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putLong("KEY_USER_ID", userId ?: -1L)
            apply()
        }
    }
}