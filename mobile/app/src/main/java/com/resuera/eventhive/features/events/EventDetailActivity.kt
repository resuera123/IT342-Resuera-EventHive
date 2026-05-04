package com.resuera.eventhive.features.events

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.resuera.eventhive.R
import com.resuera.eventhive.shared.network.RetrofitClient
import com.resuera.eventhive.shared.ui.DialogHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class EventDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        val eventId = intent.getLongExtra("EVENT_ID", 0)
        val title = intent.getStringExtra("EVENT_TITLE") ?: ""
        val desc = intent.getStringExtra("EVENT_DESC") ?: ""
        val start = intent.getStringExtra("EVENT_START") ?: ""
        val end = intent.getStringExtra("EVENT_END") ?: ""
        val location = intent.getStringExtra("EVENT_LOCATION") ?: ""
        val category = intent.getStringExtra("EVENT_CATEGORY") ?: ""
        val imageUrl = intent.getStringExtra("EVENT_IMAGE")
        val max = intent.getIntExtra("EVENT_MAX", 0)
        val status = intent.getStringExtra("EVENT_STATUS") ?: "UPCOMING"
        val organizerId = intent.getLongExtra("EVENT_ORGANIZER_ID", 0)
        val organizerName = intent.getStringExtra("EVENT_ORGANIZER_NAME") ?: ""
        var participantCount = intent.getIntExtra("EVENT_PARTICIPANT_COUNT", 0)
        var isRegistered = intent.getBooleanExtra("EVENT_IS_REGISTERED", false)

        val prefs = getSharedPreferences("EventHivePrefs", MODE_PRIVATE)
        val userId = prefs.getLong("KEY_USER_ID", -1)
        val isOrganizer = userId == organizerId

        // Bind views
        val ivImage = findViewById<ImageView>(R.id.ivDetailImage)
        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvOrganizer = findViewById<TextView>(R.id.tvDetailOrganizer)
        val tvDesc = findViewById<TextView>(R.id.tvDetailDesc)
        val tvDate = findViewById<TextView>(R.id.tvDetailDate)
        val tvTime = findViewById<TextView>(R.id.tvDetailTime)
        val tvLocation = findViewById<TextView>(R.id.tvDetailLocation)
        val tvAttendance = findViewById<TextView>(R.id.tvDetailAttendance)
        val tvStatus = findViewById<TextView>(R.id.tvDetailStatus)
        val tvCategory = findViewById<TextView>(R.id.tvDetailCategory)
        val progressBar = findViewById<ProgressBar>(R.id.progressCapacity)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        tvTitle.text = title
        tvOrganizer.text = "Organized by $organizerName"
        tvDesc.text = desc
        tvLocation.text = location
        tvStatus.text = status
        tvCategory.text = category

        // Status & category badges (using shared EventColors)
        tvStatus.setBackgroundColor(EventColors.getStatusColor(status))
        tvCategory.setBackgroundColor(EventColors.getCategoryColor(category))

        // Date/Time
        try {
            val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val dateFmt = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val startD = isoFmt.parse(start)
            val endD = isoFmt.parse(end)
            tvDate.text = dateFmt.format(startD!!)
            tvTime.text = "${timeFmt.format(startD)} – ${timeFmt.format(endD!!)}"
        } catch (e: Exception) {
            tvDate.text = start
            tvTime.text = "$start – $end"
        }

        // Attendance
        tvAttendance.text = "$participantCount / $max registered"
        if (max > 0) {
            progressBar.max = max
            progressBar.progress = participantCount
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }

        // Image
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this).load("${RetrofitClient.getBaseUrl()}$imageUrl").centerCrop().into(ivImage)
        }

        // Register button
        updateRegisterButton(btnRegister, isOrganizer, isRegistered, participantCount >= max && max > 0, status == "CANCELLED")

        btnRegister.setOnClickListener {
            btnRegister.isEnabled = false
            RetrofitClient.eventsApi.registerForEvent(eventId).enqueue(object : Callback<EventResponse> {
                override fun onResponse(call: Call<EventResponse>, response: Response<EventResponse>) {
                    if (response.isSuccessful) {
                        isRegistered = true
                        participantCount++
                        tvAttendance.text = "$participantCount / $max registered"
                        progressBar.progress = participantCount
                        updateRegisterButton(btnRegister, isOrganizer, true, participantCount >= max && max > 0, false)
                        DialogHelper.showSuccess(
                            this@EventDetailActivity,
                            "Registration Successful!",
                            "You have successfully registered for \"$title\". This event has been added to your event list."
                        )
                    } else {
                        btnRegister.isEnabled = true
                        Toast.makeText(this@EventDetailActivity, "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<EventResponse>, t: Throwable) {
                    btnRegister.isEnabled = true
                    Toast.makeText(this@EventDetailActivity, "Connection error", Toast.LENGTH_SHORT).show()
                }
            })
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun updateRegisterButton(btn: Button, isOrganizer: Boolean, isRegistered: Boolean, isFull: Boolean, isCancelled: Boolean) {
        when {
            isOrganizer -> { btn.text = "Your Event"; btn.isEnabled = false }
            isCancelled -> { btn.text = "Event Cancelled"; btn.isEnabled = false }
            isRegistered -> { btn.text = "Already Registered"; btn.isEnabled = false }
            isFull -> { btn.text = "Event Full"; btn.isEnabled = false }
            else -> { btn.text = "Register for Event"; btn.isEnabled = true }
        }
    }
}