package com.resuera.eventhive.model

data class AuthResponse(
    val status: String?,
    val id: Long?,
    val firstname: String?,
    val lastname: String?,
    val email: String?,
    val role: String?,
    val createdAt: String?,
    val profilePicUrl: String?
)