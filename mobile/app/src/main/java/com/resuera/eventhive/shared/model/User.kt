package com.resuera.eventhive.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val userID: Long?,
    @SerializedName("email")
    val userEmail: String,
    @SerializedName("password")
    val userPassword: String?,
    @SerializedName("firstname")
    val userFirstName: String?,
    @SerializedName("lastname")
    val userLastName: String?
)
