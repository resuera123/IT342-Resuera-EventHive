package com.resuera.eventhive.features.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.resuera.eventhive.R
import com.resuera.eventhive.shared.network.RetrofitClient
import com.resuera.eventhive.shared.model.AuthResponse
import com.resuera.eventhive.shared.model.User
import com.resuera.eventhive.features.dashboard.DashboardActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val email = account.email ?: return@registerForActivityResult
            val name = account.displayName ?: ""
            val parts = name.split(" ")
            val firstName = parts.firstOrNull() ?: ""
            val lastName = if (parts.size > 1) parts.last() else ""

            // Send to backend
            val body = mapOf("email" to email, "firstname" to firstName, "lastname" to lastName)
            RetrofitClient.authApi.googleLogin(body).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful) {
                        val auth = response.body()
                        if (auth?.id != null) {
                            saveUserSession(auth)
                            Toast.makeText(this@LoginActivity, "Welcome, ${auth.firstname}!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                            finish()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Google login failed", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Connection error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        } catch (e: ApiException) {
            Toast.makeText(this, "Google sign-in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Auto-login check
        val prefs = getSharedPreferences("EventHivePrefs", MODE_PRIVATE)
        if (prefs.getLong("KEY_USER_ID", -1L) != -1L) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        // Google Sign-In setup
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken("531184883938-fkfnahlgr44d6hdd8lo8it3216jrmts1.apps.googleusercontent.com")
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val emailField = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordField = findViewById<TextInputEditText>(R.id.passwordInput)
        val loginBtn = findViewById<Button>(R.id.loginButton)
        val btnRegister = findViewById<TextView>(R.id.btnRegister)
        val btnGoogle = findViewById<Button>(R.id.googleLoginButton)

        loginBtn.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginBtn.isEnabled = false
            val loginRequest = User(null, email, password, null, null)

            RetrofitClient.authApi.login(loginRequest).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    loginBtn.isEnabled = true
                    if (response.isSuccessful) {
                        val auth = response.body()
                        if (auth?.id != null) {
                            saveUserSession(auth)
                            Toast.makeText(this@LoginActivity, "Welcome, ${auth.firstname}!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, auth?.status ?: "Login failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    loginBtn.isEnabled = true
                    Toast.makeText(this@LoginActivity, "Connection Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        btnGoogle.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun saveUserSession(auth: AuthResponse) {
        val prefs = getSharedPreferences("EventHivePrefs", MODE_PRIVATE)
        prefs.edit().apply {
            putLong("KEY_USER_ID", auth.id ?: -1L)
            putString("KEY_FIRSTNAME", auth.firstname)
            putString("KEY_LASTNAME", auth.lastname)
            putString("KEY_EMAIL", auth.email)
            putString("KEY_ROLE", auth.role)
            putString("KEY_CREATED_AT", auth.createdAt)
            putString("KEY_PROFILE_PIC_URL", auth.profilePicUrl)
            apply()
        }
    }
}