package com.resuera.eventhive.features.auth

import com.resuera.eventhive.shared.model.AuthResponse
import com.resuera.eventhive.shared.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("/api/auth/login")
    fun login(@Body user: User): Call<AuthResponse>

    @POST("/api/auth/register")
    fun register(@Body user: User): Call<AuthResponse>

    @POST("/api/auth/google-mobile")
    fun googleLogin(@Body body: Map<String, String>): Call<AuthResponse>

    @GET("/api/auth/me")
    fun getMe(): Call<AuthResponse>
}