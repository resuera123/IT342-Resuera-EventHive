package com.resuera.eventhive.api

import com.resuera.eventhive.model.AuthResponse
import com.resuera.eventhive.model.EventResponse
import com.resuera.eventhive.model.NotificationResponse
import com.resuera.eventhive.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("/api/auth/login")
    fun login(@Body user: User): Call<AuthResponse>

    @POST("/api/auth/register")
    fun register(@Body user: User): Call<AuthResponse>

    @POST("/api/auth/google-mobile")
    fun googleLogin(@Body body: Map<String, String>): Call<AuthResponse>

    @GET("/api/auth/me")
    fun getMe(): Call<AuthResponse>

    @GET("/api/events")
    fun getAllEvents(): Call<List<EventResponse>>

    @GET("/api/events/my-events")
    fun getMyEvents(): Call<List<EventResponse>>

    @GET("/api/events/registered")
    fun getRegisteredEvents(): Call<List<EventResponse>>

    @Multipart
    @POST("/api/events")
    fun createEvent(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("startDate") startDate: RequestBody,
        @Part("endDate") endDate: RequestBody,
        @Part("location") location: RequestBody,
        @Part("category") category: RequestBody,
        @Part("maxParticipants") maxParticipants: RequestBody,
        @Part image: MultipartBody.Part?
    ): Call<EventResponse>

    @Multipart
    @PUT("/api/events/{id}")
    fun updateEvent(
        @Path("id") id: Long,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("startDate") startDate: RequestBody,
        @Part("endDate") endDate: RequestBody,
        @Part("location") location: RequestBody,
        @Part("category") category: RequestBody,
        @Part("maxParticipants") maxParticipants: RequestBody,
        @Part image: MultipartBody.Part?
    ): Call<EventResponse>

    @POST("/api/events/{id}/register")
    fun registerForEvent(@Path("id") eventId: Long): Call<EventResponse>

    @PATCH("/api/events/{id}/status")
    fun updateEventStatus(@Path("id") id: Long, @Query("status") status: String): Call<EventResponse>

    @DELETE("/api/events/{id}")
    fun deleteEvent(@Path("id") id: Long): Call<Void>

    @GET("/api/notifications")
    fun getNotifications(): Call<List<NotificationResponse>>

    @GET("/api/notifications/unread-count")
    fun getUnreadCount(): Call<Map<String, Int>>

    @PATCH("/api/notifications/{id}/read")
    fun markAsRead(@Path("id") id: Long): Call<Void>

    @PATCH("/api/notifications/read-all")
    fun markAllAsRead(): Call<Void>

    @PUT("/api/users/profile")
    fun updateProfile(@Body body: Map<String, String>): Call<Map<String, Any>>

    @PUT("/api/users/password")
    fun changePassword(@Body body: Map<String, String>): Call<Map<String, String>>

    @Multipart
    @POST("/api/users/profile-pic")
    fun uploadProfilePic(@Part image: MultipartBody.Part): Call<Map<String, String>>

    @GET("/api/users/profile-pic")
    fun getProfilePic(): Call<Map<String, String>>
}