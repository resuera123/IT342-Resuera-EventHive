package com.resuera.eventhive.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.resuera.eventhive.R
import com.resuera.eventhive.adapter.EventAdapter
import com.resuera.eventhive.api.RetrofitClient
import com.resuera.eventhive.model.EventResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventAdapter
    private lateinit var emptyView: LinearLayout
    private lateinit var searchField: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var tvNotifBadge: TextView
    private var allEvents: List<EventResponse> = emptyList()
    private val handler = Handler(Looper.getMainLooper())
    private val categories = listOf("All Categories", "Music", "Sports", "Tech", "Arts", "Food & Drink", "Business", "Health")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val prefs = getSharedPreferences("EventHivePrefs", MODE_PRIVATE)
        val role = prefs.getString("KEY_ROLE", "participant") ?: "participant"

        recyclerView = findViewById(R.id.rvEvents)
        emptyView = findViewById(R.id.emptyView)
        searchField = findViewById(R.id.searchEditText)
        categorySpinner = findViewById(R.id.categorySpinner)
        tvNotifBadge = findViewById(R.id.tvNotifBadge)

        // Rounded role badge
        val tvRoleBadge = findViewById<TextView>(R.id.tvRoleBadge)
        tvRoleBadge.text = role.replaceFirstChar { it.uppercase() }
        if (role.lowercase() == "organizer") {
            tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_organizer)
            tvRoleBadge.setTextColor(0xFF212529.toInt())
        } else {
            tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_participant)
            tvRoleBadge.setTextColor(0xFFFFFFFF.toInt())
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = EventAdapter(emptyList()) { event ->
            val intent = Intent(this, EventDetailActivity::class.java)
            intent.putExtra("EVENT_ID", event.id)
            intent.putExtra("EVENT_TITLE", event.title)
            intent.putExtra("EVENT_DESC", event.description)
            intent.putExtra("EVENT_START", event.startDate)
            intent.putExtra("EVENT_END", event.endDate)
            intent.putExtra("EVENT_LOCATION", event.location)
            intent.putExtra("EVENT_CATEGORY", event.category)
            intent.putExtra("EVENT_IMAGE", event.imageUrl)
            intent.putExtra("EVENT_MAX", event.maxParticipants ?: 0)
            intent.putExtra("EVENT_STATUS", event.status)
            intent.putExtra("EVENT_ORGANIZER_ID", event.organizerId ?: 0L)
            intent.putExtra("EVENT_ORGANIZER_NAME", event.organizerName)
            intent.putExtra("EVENT_PARTICIPANT_COUNT", event.participantCount ?: 0)
            intent.putExtra("EVENT_IS_REGISTERED", event.isRegistered ?: false)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = spinnerAdapter
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) { filterEvents() }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { filterEvents() }
            override fun afterTextChanged(s: Editable?) {}
        })

        findViewById<ImageButton>(R.id.btnNotifications).setOnClickListener { startActivity(Intent(this, NotificationsActivity::class.java)) }
        findViewById<ImageButton>(R.id.btnProfile).setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }

        // Styled logout
        findViewById<ImageButton>(R.id.btnLogout).setOnClickListener {
            DialogHelper.showConfirm(this,
                DialogIcon.WARNING,
                "Logout",
                "Are you sure you want to log out of your account?",
                "Logout", "#DC3545"
            ) { performLogout() }
        }

        val fab = findViewById<FloatingActionButton>(R.id.fabCreateEvent)
        if (role.lowercase() == "organizer") {
            fab.visibility = View.VISIBLE
            fab.setOnClickListener { startActivity(Intent(this, CreateEventActivity::class.java)) }
        } else { fab.visibility = View.GONE }
    }

    override fun onResume() { super.onResume(); loadEvents(); pollUnreadCount() }
    override fun onPause() { super.onPause(); handler.removeCallbacksAndMessages(null) }

    private fun loadEvents() {
        RetrofitClient.instance.getAllEvents().enqueue(object : Callback<List<EventResponse>> {
            override fun onResponse(call: Call<List<EventResponse>>, response: Response<List<EventResponse>>) {
                if (response.isSuccessful) { allEvents = response.body() ?: emptyList(); filterEvents() }
            }
            override fun onFailure(call: Call<List<EventResponse>>, t: Throwable) {
                Toast.makeText(this@DashboardActivity, "Failed to load events", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterEvents() {
        val query = searchField.text.toString().lowercase()
        val cat = categorySpinner.selectedItem?.toString() ?: "All Categories"
        val filtered = allEvents.filter { e ->
            if (e.status == "COMPLETED" || e.status == "CANCELLED") return@filter false
            val matchSearch = query.isEmpty() || e.title.lowercase().contains(query) || (e.description?.lowercase()?.contains(query) == true)
            val matchCat = cat == "All Categories" || e.category == cat
            matchSearch && matchCat
        }
        adapter.updateList(filtered)
        emptyView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun pollUnreadCount() {
        RetrofitClient.instance.getUnreadCount().enqueue(object : Callback<Map<String, Int>> {
            override fun onResponse(call: Call<Map<String, Int>>, response: Response<Map<String, Int>>) {
                val count = response.body()?.get("count") ?: 0
                tvNotifBadge.text = if (count > 9) "9+" else count.toString()
                tvNotifBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
            }
            override fun onFailure(call: Call<Map<String, Int>>, t: Throwable) {}
        })
        handler.postDelayed({ pollUnreadCount() }, 30000)
    }

    private fun performLogout() {
        // Preserve profile pics before clearing
        val prefs = getSharedPreferences("EventHivePrefs", Context.MODE_PRIVATE)
        val profilePics = prefs.all.filter { it.key.startsWith("KEY_PROFILE_PIC_") }
        prefs.edit().clear().apply()
        // Restore profile pics
        val editor = prefs.edit()
        profilePics.forEach { (key, value) -> editor.putString(key, value as? String) }
        editor.apply()

        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}