package com.resuera.eventhive.features.notifications

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface NotificationsApi {

    @GET("/api/notifications")
    fun getNotifications(): Call<List<NotificationResponse>>

    @GET("/api/notifications/unread-count")
    fun getUnreadCount(): Call<Map<String, Int>>

    @PATCH("/api/notifications/{id}/read")
    fun markAsRead(@Path("id") id: Long): Call<Void>

    @PATCH("/api/notifications/read-all")
    fun markAllAsRead(): Call<Void>
}