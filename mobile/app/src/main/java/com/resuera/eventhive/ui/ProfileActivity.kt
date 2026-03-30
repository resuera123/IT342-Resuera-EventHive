package com.resuera.eventhive.ui

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.resuera.eventhive.R

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tvFirstName = findViewById<TextView>(R.id.tvDisplayFirstName)
        val tvLastName = findViewById<TextView>(R.id.tvDisplayLastName)
        val tvEmail = findViewById<TextView>(R.id.tvDisplayEmail)
        val btnBack = findViewById<ImageButton>(R.id.btnBackToDashboard)

        // 1. Get Data from Intent
        val firstName = intent.getStringExtra("USER_FIRSTNAME") ?: "N/A"
        val lastName = intent.getStringExtra("USER_LASTNAME") ?: "N/A"
        val rawEmail = intent.getStringExtra("USER_EMAIL") ?: "N/A"

        // 2. Display Data (with masked email)
        tvFirstName.text = "$firstName"
        tvLastName.text = "$lastName"
        tvEmail.text = "${maskEmail(rawEmail)}"

        // 3. Back Button Logic
        btnBack.setOnClickListener {
            // finish() simply pops this activity off the stack
            // and returns the user to the DashboardActivity.
            finish()
        }
    }

    private fun maskEmail(email: String): String {
        return if (email.contains("@") && email.length > 3) {
            val parts = email.split("@")
            val firstThree = parts[0].take(3)
            "$firstThree...@${parts[1]}"
        } else {
            email
        }
    }
}