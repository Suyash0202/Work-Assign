package com.example.workassign.Data

import com.google.gson.annotations.SerializedName

data class FCMMessage(
    @SerializedName("message") val message: MessageBody
)

