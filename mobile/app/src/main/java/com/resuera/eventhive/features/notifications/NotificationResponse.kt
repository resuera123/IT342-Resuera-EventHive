package com.resuera.eventhive.features.notifications

data class NotificationResponse(
    val id: Long,
    val type: String,
    val title: String,
    val message: String,
    val read: Boolean,
    val eventId: Long,
    val createdAt: String
)