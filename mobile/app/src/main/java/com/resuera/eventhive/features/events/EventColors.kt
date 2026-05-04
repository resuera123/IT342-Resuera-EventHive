package com.resuera.eventhive.features.events

import android.graphics.Color

/**
 * Centralized category and status color mappings for events.
 * Used by EventAdapter, ProfileActivity, and EventDetailActivity.
 */
object EventColors {

    fun getStatusColor(status: String?): Int = Color.parseColor(when (status) {
        "UPCOMING" -> "#0d6efd"
        "ONGOING" -> "#198754"
        "CANCELLED" -> "#DC3545"
        "COMPLETED" -> "#6c757d"
        else -> "#6c757d"
    })

    fun getCategoryColor(category: String?): Int = Color.parseColor(when (category) {
        "Music" -> "#7c3aed"
        "Sports" -> "#ea580c"
        "Tech" -> "#0891b2"
        "Arts" -> "#db2777"
        "Food & Drink" -> "#d97706"
        "Business" -> "#1e40af"
        "Health" -> "#059669"
        else -> "#6c757d"
    })

    val CATEGORIES = listOf(
        "Music", "Sports", "Tech", "Arts",
        "Food & Drink", "Business", "Health"
    )
}