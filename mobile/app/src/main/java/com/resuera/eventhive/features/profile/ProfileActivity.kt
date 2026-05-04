package com.resuera.eventhive.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.resuera.eventhive.R
import com.resuera.eventhive.features.events.EditEventActivity
import com.resuera.eventhive.shared.network.RetrofitClient
import com.resuera.eventhive.features.events.EventResponse
import com.resuera.eventhive.shared.ui.DialogHelper
import com.resuera.eventhive.shared.ui.DialogIcon
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var rvEvents: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var ivProfilePic: ImageView
    private lateinit var tvInitials: TextView
    private var ownEvents: List<EventResponse> = emptyList()
    private var registeredEvents: List<EventResponse> = emptyList()
    private var currentTab = "own"
    private var isOrganizer = false

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            uploadProfilePic(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val prefs = getSharedPreferences("EventHivePrefs", MODE_PRIVATE)
        val firstName = prefs.getString("KEY_FIRSTNAME", "") ?: ""
        val lastName = prefs.getString("KEY_LASTNAME", "") ?: ""
        val email = prefs.getString("KEY_EMAIL", "") ?: ""
        val role = prefs.getString("KEY_ROLE", "participant") ?: "participant"
        val profilePicUrl = prefs.getString("KEY_PROFILE_PIC_URL", null)
        isOrganizer = role.lowercase() == "organizer"

        tvInitials = findViewById(R.id.tvInitials)
        ivProfilePic = findViewById(R.id.ivProfilePic)
        tvInitials.text = "${firstName.firstOrNull()?.uppercase() ?: ""}${lastName.firstOrNull()?.uppercase() ?: ""}"
        findViewById<TextView>(R.id.tvProfileName).text = "$firstName $lastName"
        findViewById<TextView>(R.id.tvProfileEmail).text = email
        findViewById<TextView>(R.id.tvProfileRole).text = role.replaceFirstChar { it.uppercase() }

        loadProfilePic(profilePicUrl)

        findViewById<FrameLayout>(R.id.avatarContainer).setOnClickListener {
            pickImageLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"; addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            })
        }

        val btnOwn = findViewById<Button>(R.id.btnTabOwn)
        val btnMy = findViewById<Button>(R.id.btnTabMy)
        val btnPast = findViewById<Button>(R.id.btnTabPast)
        if (!isOrganizer) { btnOwn.visibility = View.GONE; currentTab = "my" }

        rvEvents = findViewById(R.id.rvProfileEvents)
        tvEmpty = findViewById(R.id.tvProfileEmpty)
        rvEvents.layoutManager = LinearLayoutManager(this)

        val selectTab = { tab: String ->
            currentTab = tab
            btnOwn.setBackgroundColor(if (tab == "own") 0xFF212529.toInt() else 0xFFFFFFFF.toInt())
            btnOwn.setTextColor(if (tab == "own") 0xFFFFFFFF.toInt() else 0xFF212529.toInt())
            btnMy.setBackgroundColor(if (tab == "my") 0xFF212529.toInt() else 0xFFFFFFFF.toInt())
            btnMy.setTextColor(if (tab == "my") 0xFFFFFFFF.toInt() else 0xFF212529.toInt())
            btnPast.setBackgroundColor(if (tab == "past") 0xFF212529.toInt() else 0xFFFFFFFF.toInt())
            btnPast.setTextColor(if (tab == "past") 0xFFFFFFFF.toInt() else 0xFF212529.toInt())
            showCurrentTab()
        }
        btnOwn.setOnClickListener { selectTab("own") }
        btnMy.setOnClickListener { selectTab("my") }
        btnPast.setOnClickListener { selectTab("past") }
        findViewById<ImageButton>(R.id.btnBackToDashboard).setOnClickListener { finish() }
        selectTab(currentTab)
    }

    override fun onResume() { super.onResume(); loadData() }

    private fun loadProfilePic(url: String?) {
        if (!url.isNullOrEmpty()) {
            Glide.with(this).load("${RetrofitClient.getBaseUrl()}$url").circleCrop().into(ivProfilePic)
            tvInitials.visibility = View.GONE; ivProfilePic.visibility = View.VISIBLE
        }
    }

    private fun uploadProfilePic(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return
            val file = File(cacheDir, "profile_pic_upload.jpg")
            FileOutputStream(file).use { out -> inputStream.copyTo(out) }
            val requestBody = file.asRequestBody("image/*".toMediaType())
            val part = MultipartBody.Part.createFormData("image", file.name, requestBody)

            RetrofitClient.instance.uploadProfilePic(part).enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                    if (response.isSuccessful) {
                        val picUrl = response.body()?.get("profilePicUrl") ?: ""
                        getSharedPreferences("EventHivePrefs", MODE_PRIVATE).edit()
                            .putString("KEY_PROFILE_PIC_URL", picUrl).apply()
                        loadProfilePic(picUrl)
                        DialogHelper.showSuccess(this@ProfileActivity, "Photo Updated", "Your profile picture has been updated.")
                    }
                }
                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadData() {
        RetrofitClient.instance.getRegisteredEvents().enqueue(object : Callback<List<EventResponse>> {
            override fun onResponse(call: Call<List<EventResponse>>, r: Response<List<EventResponse>>) {
                registeredEvents = r.body() ?: emptyList(); showCurrentTab()
            }
            override fun onFailure(call: Call<List<EventResponse>>, t: Throwable) {}
        })
        if (isOrganizer) {
            RetrofitClient.instance.getMyEvents().enqueue(object : Callback<List<EventResponse>> {
                override fun onResponse(call: Call<List<EventResponse>>, r: Response<List<EventResponse>>) {
                    Log.d("ProfileActivity", "Own events loaded: ${r.code()}, count: ${r.body()?.size ?: 0}")
                    ownEvents = r.body() ?: emptyList()
                    ownEvents.forEach { Log.d("ProfileActivity", "Event: id=${it.id}, title=${it.title}") }
                    showCurrentTab()
                }
                override fun onFailure(call: Call<List<EventResponse>>, t: Throwable) {
                    Log.e("ProfileActivity", "Failed to load own events: ${t.message}")
                }
            })
        }
    }

    private fun showCurrentTab() {
        val list = when (currentTab) {
            "own" -> ownEvents
            "my" -> registeredEvents.filter { it.status == "UPCOMING" || it.status == "ONGOING" }
            "past" -> registeredEvents.filter { it.status == "CANCELLED" || it.status == "COMPLETED" }
            else -> emptyList()
        }
        rvEvents.adapter = if (currentTab == "own" && isOrganizer) OwnEventAdapter(list) else SimpleEventAdapter(list)
        tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        rvEvents.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
    }

    // ── Status badge color helper ──
    private fun applyStatusColor(tvStatus: TextView, status: String?) {
        val color = when (status) {
            "UPCOMING" -> "#0d6efd"
            "ONGOING" -> "#198754"
            "CANCELLED" -> "#DC3545"
            "COMPLETED" -> "#6c757d"
            else -> "#6c757d"
        }
        val bg = GradientDrawable().apply {
            setColor(Color.parseColor(color))
            cornerRadius = 8f
        }
        tvStatus.background = bg
        tvStatus.setTextColor(Color.WHITE)
    }

    private fun applyCategoryColor(tvCat: TextView, category: String?) {
        val color = when (category) {
            "Music" -> "#7c3aed"; "Sports" -> "#ea580c"; "Tech" -> "#0891b2"
            "Arts" -> "#db2777"; "Food & Drink" -> "#d97706"; "Business" -> "#1e40af"
            "Health" -> "#059669"; else -> "#6c757d"
        }
        val bg = GradientDrawable().apply {
            setColor(Color.parseColor(color))
            cornerRadius = 8f
        }
        tvCat.background = bg
        tvCat.setTextColor(Color.WHITE)
    }

    // ── Organizer Actions ──

    private fun confirmCancel(e: EventResponse) {
        DialogHelper.showConfirm(this, DialogIcon.WARNING, "Cancel Event",
            "Are you sure you want to cancel \"${e.title}\"?\n\nParticipants will be notified.",
            "Cancel Event", "#D97706") {
            RetrofitClient.instance.updateEventStatus(e.id, "CANCELLED").enqueue(object : Callback<EventResponse> {
                override fun onResponse(call: Call<EventResponse>, r: Response<EventResponse>) {
                    if (r.isSuccessful) DialogHelper.showSuccess(this@ProfileActivity, "Event Cancelled", "\"${e.title}\" has been cancelled.") { loadData() }
                }
                override fun onFailure(call: Call<EventResponse>, t: Throwable) { Toast.makeText(this@ProfileActivity, "Connection error", Toast.LENGTH_SHORT).show() }
            })
        }
    }

    private fun confirmContinue(e: EventResponse) {
        DialogHelper.showConfirm(this, DialogIcon.INFO, "Continue Event",
            "Resume \"${e.title}\" and set its status back to Upcoming?",
            "Continue Event", "#198754") {
            RetrofitClient.instance.updateEventStatus(e.id, "UPCOMING").enqueue(object : Callback<EventResponse> {
                override fun onResponse(call: Call<EventResponse>, r: Response<EventResponse>) {
                    if (r.isSuccessful) DialogHelper.showSuccess(this@ProfileActivity, "Event Resumed", "\"${e.title}\" is now Upcoming.") { loadData() }
                }
                override fun onFailure(call: Call<EventResponse>, t: Throwable) { Toast.makeText(this@ProfileActivity, "Connection error", Toast.LENGTH_SHORT).show() }
            })
        }
    }

    private fun confirmDelete(e: EventResponse) {
        DialogHelper.showConfirm(this, DialogIcon.DANGER, "Delete Event",
            "Permanently delete \"${e.title}\"?\n\nThis cannot be undone.",
            "Delete Permanently", "#DC3545") {
            RetrofitClient.instance.deleteEvent(e.id).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, r: Response<Void>) {
                    if (r.isSuccessful) DialogHelper.showSuccess(this@ProfileActivity, "Event Deleted", "\"${e.title}\" has been deleted.") { loadData() }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) { Toast.makeText(this@ProfileActivity, "Connection error", Toast.LENGTH_SHORT).show() }
            })
        }
    }

    private fun openEditEvent(e: EventResponse) {
        startActivity(Intent(this, EditEventActivity::class.java).apply {
            putExtra("EVENT_ID", e.id); putExtra("EVENT_TITLE", e.title)
            putExtra("EVENT_DESC", e.description); putExtra("EVENT_START", e.startDate)
            putExtra("EVENT_END", e.endDate); putExtra("EVENT_LOCATION", e.location)
            putExtra("EVENT_CATEGORY", e.category); putExtra("EVENT_MAX", e.maxParticipants ?: 0)
        })
    }

    // ── Adapters ──

    inner class OwnEventAdapter(private val events: List<EventResponse>) : RecyclerView.Adapter<OwnEventAdapter.VH>() {
        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tvOwnTitle)
            val tvDate: TextView = view.findViewById(R.id.tvOwnDate)
            val tvStatus: TextView = view.findViewById(R.id.tvOwnStatus)
            val tvCategory: TextView = view.findViewById(R.id.tvOwnCategory)
            val tvParticipants: TextView = view.findViewById(R.id.tvOwnParticipants)
            val btnEdit: Button = view.findViewById(R.id.btnOwnEdit)
            val btnCancel: Button = view.findViewById(R.id.btnOwnCancel)
            val btnContinue: Button = view.findViewById(R.id.btnOwnContinue)
            val btnDelete: Button = view.findViewById(R.id.btnOwnDelete)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(LayoutInflater.from(parent.context).inflate(R.layout.item_own_event, parent, false))
        override fun onBindViewHolder(holder: VH, position: Int) {
            val e = events[position]
            holder.tvTitle.text = e.title
            holder.tvParticipants.text = "${e.participantCount ?: 0}/${e.maxParticipants ?: "∞"} participants"
            try {
                val f = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val d = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
                holder.tvDate.text = d.format(f.parse(e.startDate ?: "")!!)
            } catch (_: Exception) { holder.tvDate.text = e.startDate ?: "" }

            // Status + category badge colors
            holder.tvStatus.text = e.status ?: "UPCOMING"
            applyStatusColor(holder.tvStatus, e.status)
            holder.tvCategory.text = e.category ?: ""
            applyCategoryColor(holder.tvCategory, e.category)

            val canCancel = e.status == "UPCOMING" || e.status == "ONGOING"
            val canContinue = e.status == "CANCELLED"
            val canDelete = e.status == "CANCELLED" || e.status == "COMPLETED"
            holder.btnEdit.visibility = if (canCancel) View.VISIBLE else View.GONE
            holder.btnCancel.visibility = if (canCancel) View.VISIBLE else View.GONE
            holder.btnContinue.visibility = if (canContinue) View.VISIBLE else View.GONE
            holder.btnDelete.visibility = if (canDelete) View.VISIBLE else View.GONE

            holder.btnEdit.setOnClickListener { openEditEvent(e) }
            holder.btnCancel.setOnClickListener { confirmCancel(e) }
            holder.btnContinue.setOnClickListener { confirmContinue(e) }
            holder.btnDelete.setOnClickListener { confirmDelete(e) }
        }
        override fun getItemCount() = events.size
    }

    inner class SimpleEventAdapter(private val events: List<EventResponse>) : RecyclerView.Adapter<SimpleEventAdapter.VH>() {
        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tvOwnTitle)
            val tvDate: TextView = view.findViewById(R.id.tvOwnDate)
            val tvStatus: TextView = view.findViewById(R.id.tvOwnStatus)
            val tvCategory: TextView = view.findViewById(R.id.tvOwnCategory)
            val tvParticipants: TextView = view.findViewById(R.id.tvOwnParticipants)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_own_event, parent, false)
            v.findViewById<LinearLayout>(R.id.layoutActions).visibility = View.GONE
            return VH(v)
        }
        override fun onBindViewHolder(holder: VH, position: Int) {
            val e = events[position]
            holder.tvTitle.text = e.title
            holder.tvParticipants.text = "${e.participantCount ?: 0}/${e.maxParticipants ?: "∞"} participants"
            try {
                val f = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val d = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
                holder.tvDate.text = d.format(f.parse(e.startDate ?: "")!!)
            } catch (_: Exception) { holder.tvDate.text = e.startDate ?: "" }
            holder.tvStatus.text = e.status ?: ""
            applyStatusColor(holder.tvStatus, e.status)
            holder.tvCategory.text = e.category ?: ""
            applyCategoryColor(holder.tvCategory, e.category)
        }
        override fun getItemCount() = events.size
    }
}