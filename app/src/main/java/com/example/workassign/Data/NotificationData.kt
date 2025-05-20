package com.example.workassign.Data

import com.google.gson.annotations.SerializedName

data class NotificationData(
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String
)
