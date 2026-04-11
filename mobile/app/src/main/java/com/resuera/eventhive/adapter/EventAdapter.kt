package com.resuera.eventhive.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.resuera.eventhive.R
import com.resuera.eventhive.api.RetrofitClient
import com.resuera.eventhive.model.EventResponse
import java.text.SimpleDateFormat
import java.util.Locale

class EventAdapter(
    private var events: List<EventResponse>,
    private val onClick: (EventResponse) -> Unit
) : RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView = view.findViewById(R.id.cardEvent)
        val ivImage: ImageView = view.findViewById(R.id.ivEventImage)
        val tvTitle: TextView = view.findViewById(R.id.tvEventTitle)
        val tvDateTime: TextView = view.findViewById(R.id.tvEventDateTime)
        val tvLocation: TextView = view.findViewById(R.id.tvEventLocation)
        val tvParticipants: TextView = view.findViewById(R.id.tvEventParticipants)
        val tvStatus: TextView = view.findViewById(R.id.tvEventStatus)
        val tvCategory: TextView = view.findViewById(R.id.tvEventCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = events[position]

        holder.tvTitle.text = event.title

        // Date/Time
        try {
            val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val displayDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val displayTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = isoFmt.parse(event.startDate ?: "")
            holder.tvDateTime.text = "${displayDate.format(date!!)} • ${displayTime.format(date)}"
        } catch (e: Exception) {
            holder.tvDateTime.text = event.startDate ?: ""
        }

        holder.tvLocation.text = event.location ?: ""
        holder.tvParticipants.text = "${event.participantCount ?: 0}/${event.maxParticipants ?: "∞"}"

        // Status badge
        holder.tvStatus.text = event.status ?: "UPCOMING"
        when (event.status) {
            "UPCOMING" -> holder.tvStatus.setBackgroundColor(Color.parseColor("#0d6efd"))
            "ONGOING" -> holder.tvStatus.setBackgroundColor(Color.parseColor("#198754"))
            else -> holder.tvStatus.setBackgroundColor(Color.parseColor("#6c757d"))
        }

        // Category badge
        holder.tvCategory.text = event.category ?: ""
        holder.tvCategory.setBackgroundColor(getCategoryColor(event.category))

        // Image
        if (!event.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.ivImage.context)
                .load("${RetrofitClient.getBaseUrl()}${event.imageUrl}")
                .centerCrop()
                .into(holder.ivImage)
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_calendar_empty)
            holder.ivImage.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        holder.card.setOnClickListener { onClick(event) }
    }

    override fun getItemCount() = events.size

    fun updateList(newEvents: List<EventResponse>) {
        events = newEvents
        notifyDataSetChanged()
    }

    private fun getCategoryColor(cat: String?): Int {
        return when (cat) {
            "Music" -> Color.parseColor("#7c3aed")
            "Sports" -> Color.parseColor("#ea580c")
            "Tech" -> Color.parseColor("#0891b2")
            "Arts" -> Color.parseColor("#db2777")
            "Food & Drink" -> Color.parseColor("#d97706")
            "Business" -> Color.parseColor("#1e40af")
            "Health" -> Color.parseColor("#059669")
            else -> Color.parseColor("#6c757d")
        }
    }
}