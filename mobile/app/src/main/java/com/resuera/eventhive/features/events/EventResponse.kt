package com.resuera.eventhive.features.events

data class EventResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val startDate: String?,
    val endDate: String?,
    val location: String?,
    val category: String?,
    val imageUrl: String?,
    val maxParticipants: Int?,
    val status: String?,
    val organizerId: Long?,
    val organizerName: String?,
    val createdAt: String?,
    val participantCount: Int?,
    val isRegistered: Boolean?
)