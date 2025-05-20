package com.example.workassign.Data

import com.google.gson.annotations.SerializedName

data class MessageBody(
    @SerializedName("token") val token: String,
    @SerializedName("android") val android: AndroidConfig,
    @SerializedName("data") val data: Map<String, String>
)
