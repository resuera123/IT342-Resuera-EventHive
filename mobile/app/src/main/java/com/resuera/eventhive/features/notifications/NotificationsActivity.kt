package com.resuera.eventhive.ui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.resuera.eventhive.R
import com.resuera.eventhive.shared.network.RetrofitClient
import com.resuera.eventhive.features.notifications.NotificationResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class NotificationsActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var tvEmpty: TextView
    private lateinit var ivEmptyIcon: ImageView
    private lateinit var btnMarkAll: Button
    private var allNotifications: MutableList<NotificationResponse> = mutableListOf()

    // Map notification type to preference key
    private val typeToPrefKey = mapOf(
        "REGISTRATION" to "registration",
        "NEW_PARTICIPANT" to "newParticipant",
        "EVENT_CANCELLED" to "cancellation",
        "EVENT_RESUMED" to "eventUpdates"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        listView = findViewById(R.id.lvNotifications)
        tvEmpty = findViewById(R.id.tvNoNotifications)
        ivEmptyIcon = findViewById(R.id.ivEmptyNotifIcon)
        btnMarkAll = findViewById(R.id.btnMarkAllRead)

        findViewById<ImageButton>(R.id.btnBackFromNotifs).setOnClickListener { finish() }

        btnMarkAll.setOnClickListener {
            RetrofitClient.instance.markAllAsRead().enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, r: Response<Void>) {
                    allNotifications.forEachIndexed { i, n -> allNotifications[i] = n.copy(read = true) }
                    refreshList()
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {}
            })
        }

        loadNotifications()
    }

    private fun getFilteredNotifications(): List<NotificationResponse> {
        val prefs = getSharedPreferences("EventHiveNotifPrefs", MODE_PRIVATE)
        return allNotifications.filter { notif ->
            val prefKey = typeToPrefKey[notif.type] ?: return@filter true
            prefs.getBoolean(prefKey, true)
        }
    }

    private fun loadNotifications() {
        RetrofitClient.instance.getNotifications().enqueue(object : Callback<List<NotificationResponse>> {
            override fun onResponse(call: Call<List<NotificationResponse>>, r: Response<List<NotificationResponse>>) {
                allNotifications = (r.body() ?: emptyList()).toMutableList()
                refreshList()
            }
            override fun onFailure(call: Call<List<NotificationResponse>>, t: Throwable) {
                Toast.makeText(this@NotificationsActivity, "Failed to load", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun refreshList() {
        val filtered = getFilteredNotifications()
        val hasUnread = filtered.any { !it.read }

        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        ivEmptyIcon.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        listView.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
        btnMarkAll.visibility = if (hasUnread) View.VISIBLE else View.GONE

        listView.adapter = object : ArrayAdapter<NotificationResponse>(this, 0, filtered) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: layoutInflater.inflate(R.layout.item_notification, parent, false)
                val n = filtered[position]

                view.findViewById<TextView>(R.id.tvNotifTitle).apply {
                    text = n.title; setTypeface(null, if (n.read) Typeface.NORMAL else Typeface.BOLD)
                }
                view.findViewById<TextView>(R.id.tvNotifMessage).text = n.message
                view.findViewById<TextView>(R.id.tvNotifTime).text = timeAgo(n.createdAt)
                view.findViewById<View>(R.id.viewUnreadDot).visibility = if (n.read) View.GONE else View.VISIBLE
                view.setBackgroundColor(if (n.read) Color.WHITE else Color.parseColor("#F8F9FF"))

                val iconBg = view.findViewById<View>(R.id.viewNotifIconBg)
                val tvIcon = view.findViewById<TextView>(R.id.tvNotifIcon)
                when (n.type) {
                    "REGISTRATION" -> { iconBg.setBackgroundResource(R.drawable.bg_notif_registration); tvIcon.text = "✓"; tvIcon.setTextColor(Color.parseColor("#198754")) }
                    "NEW_PARTICIPANT" -> { iconBg.setBackgroundResource(R.drawable.bg_notif_info); tvIcon.text = "👤"; tvIcon.setTextColor(Color.parseColor("#0891b2")) }
                    "EVENT_CANCELLED" -> { iconBg.setBackgroundResource(R.drawable.bg_notif_cancelled); tvIcon.text = "✕"; tvIcon.setTextColor(Color.parseColor("#DC3545")) }
                    "EVENT_RESUMED" -> { iconBg.setBackgroundResource(R.drawable.bg_notif_registration); tvIcon.text = "▶"; tvIcon.setTextColor(Color.parseColor("#198754")) }
                    else -> { iconBg.setBackgroundResource(R.drawable.bg_notif_info); tvIcon.text = "•"; tvIcon.setTextColor(Color.parseColor("#6c757d")) }
                }

                return view
            }
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val n = filtered[position]
            if (!n.read) {
                RetrofitClient.instance.markAsRead(n.id).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, r: Response<Void>) {
                        val idx = allNotifications.indexOfFirst { it.id == n.id }
                        if (idx >= 0) allNotifications[idx] = n.copy(read = true)
                        refreshList()
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {}
                })
            }
        }
    }

    private fun timeAgo(dateStr: String): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(dateStr) ?: return dateStr
            val diff = Date().time - date.time
            val mins = TimeUnit.MILLISECONDS.toMinutes(diff); val hrs = TimeUnit.MILLISECONDS.toHours(diff); val days = TimeUnit.MILLISECONDS.toDays(diff)
            when { mins < 1 -> "Just now"; mins < 60 -> "${mins}m ago"; hrs < 24 -> "${hrs}h ago"; days < 7 -> "${days}d ago"; else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date) }
        } catch (_: Exception) { dateStr.take(16).replace("T", " ") }
    }
}