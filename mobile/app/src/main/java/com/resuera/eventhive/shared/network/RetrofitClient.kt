package com.resuera.eventhive.shared.network

import com.resuera.eventhive.features.auth.AuthApi
import com.resuera.eventhive.features.events.EventsApi
import com.resuera.eventhive.features.notifications.NotificationsApi
import com.resuera.eventhive.features.profile.ProfileApi
import com.resuera.eventhive.features.settings.SettingsApi
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit

/**
 * Centralized Retrofit factory.
 * Each feature has its own typed API interface; they all share the same
 * underlying OkHttpClient (with cookie jar for session persistence).
 */
object RetrofitClient {

    // Use 10.0.2.2 for Android emulator, or your LAN IP for a physical device
    private const val BASE_URL = "http://192.168.254.100:8081/"

    fun getBaseUrl(): String = BASE_URL.trimEnd('/')

    private val cookieManager = CookieManager().apply {
        setCookiePolicy(CookiePolicy.ACCEPT_ALL)
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .cookieJar(JavaNetCookieJar(cookieManager))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // ── Per-feature APIs ──
    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val eventsApi: EventsApi by lazy { retrofit.create(EventsApi::class.java) }
    val notificationsApi: NotificationsApi by lazy { retrofit.create(NotificationsApi::class.java) }
    val profileApi: ProfileApi by lazy { retrofit.create(ProfileApi::class.java) }
    val settingsApi: SettingsApi by lazy { retrofit.create(SettingsApi::class.java) }
}