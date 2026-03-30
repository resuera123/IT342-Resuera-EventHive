package com.resuera.eventhive.api

import com.resuera.eventhive.model.AuthResponse
import com.resuera.eventhive.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("/api/auth/login")
    fun login(@Body user: User): Call<AuthResponse>

    @POST("/api/auth/register")
    fun register(@Body user: User): Call<AuthResponse>

    @GET("/api/auth/me")
    fun getProfile(@Query("userId") userId: Long): Call<AuthResponse>

}