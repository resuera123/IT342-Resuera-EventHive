package com.resuera.eventhive.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.resuera.eventhive.R

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        // Handle system bar padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnLogout = findViewById<ImageButton>(R.id.btnLogout)

        btnLogout.setOnClickListener {
            performLogout()
        }

        val fName = intent.getStringExtra("USER_FIRSTNAME")
        val lName = intent.getStringExtra("USER_LASTNAME")
        val email = intent.getStringExtra("USER_EMAIL")

        val btnProfile = findViewById<ImageButton>(R.id.btnProfile)

        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)

            intent.putExtra("USER_FIRSTNAME", fName)
            intent.putExtra("USER_LASTNAME", lName)
            intent.putExtra("USER_EMAIL", email)

            startActivity(intent)

        }
    }

    private fun performLogout() {
        // 1. Clear SharedPreferences
        val sharedPref = getSharedPreferences("EventHivePrefs", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        // 2. Redirect to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)

        // This flag clears the activity stack so the user can't "Back" into the Dashboard
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}