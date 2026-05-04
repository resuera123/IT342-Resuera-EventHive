package com.resuera.eventhive.features.settings

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PUT

interface SettingsApi {

    @PUT("/api/users/profile")
    fun updateProfile(@Body body: Map<String, String>): Call<Map<String, Any>>

    @PUT("/api/users/password")
    fun changePassword(@Body body: Map<String, String>): Call<Map<String, String>>
}