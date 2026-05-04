package com.resuera.eventhive.features.dashboard

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.resuera.eventhive.R
import com.resuera.eventhive.features.events.EventColors
import com.resuera.eventhive.features.events.EventResponse
import com.resuera.eventhive.shared.network.RetrofitClient
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

        // Status badge — uses shared EventColors
        holder.tvStatus.text = event.status ?: "UPCOMING"
        val statusBg = GradientDrawable().apply {
            setColor(EventColors.getStatusColor(event.status))
            cornerRadius = 8f
        }
        holder.tvStatus.background = statusBg
        holder.tvStatus.setTextColor(Color.WHITE)

        // Category badge — uses shared EventColors
        holder.tvCategory.text = event.category ?: ""
        val catBg = GradientDrawable().apply {
            setColor(EventColors.getCategoryColor(event.category))
            cornerRadius = 8f
        }
        holder.tvCategory.background = catBg
        holder.tvCategory.setTextColor(Color.WHITE)

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
}