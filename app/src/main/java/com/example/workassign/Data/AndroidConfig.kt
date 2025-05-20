package com.example.workassign.Data

import com.google.gson.annotations.SerializedName

data class AndroidConfig(
    @SerializedName("notification") val notification: NotificationData
)
