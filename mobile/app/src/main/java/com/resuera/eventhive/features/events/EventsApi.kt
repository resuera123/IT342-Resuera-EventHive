package com.resuera.eventhive.features.events

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface EventsApi {

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
}