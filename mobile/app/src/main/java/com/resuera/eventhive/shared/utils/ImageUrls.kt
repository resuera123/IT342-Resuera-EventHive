package com.resuera.eventhive.shared.util

import com.resuera.eventhive.shared.network.RetrofitClient
object ImageUrls {

    fun resolve(imageUrl: String?): String? {
        if (imageUrl.isNullOrBlank()) return null

        // Already a full URL (Supabase or any external CDN)
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl
        }

        // Relative path from the legacy local upload code path.
        // RetrofitClient.BASE_URL ends with "/", imageUrl starts with "/", so trim one.
        val base = RetrofitClient.BASE_URL.trimEnd('/')
        return "$base$imageUrl"
    }
}