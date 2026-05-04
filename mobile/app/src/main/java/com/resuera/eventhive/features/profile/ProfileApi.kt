package com.resuera.eventhive.features.profile

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Profile feature uses its own profile-pic endpoints,
 * and uses EventsApi (via RetrofitClient.eventsApi) for events.
 */
interface ProfileApi {

    @Multipart
    @POST("/api/users/profile-pic")
    fun uploadProfilePic(@Part image: MultipartBody.Part): Call<Map<String, String>>

    @GET("/api/users/profile-pic")
    fun getProfilePic(): Call<Map<String, String>>
}