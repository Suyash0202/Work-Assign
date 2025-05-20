package com.example.workassign.Api

import com.example.workassign.Data.FCMMessage
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface FCMApiService {
    @POST("https://fcm.googleapis.com/v1/projects/job-assign/messages:send")
    @Headers("Content-Type: application/json")
    fun sendNotification(
        @Body message: FCMMessage,
        @Header("Authorization") accessToken: String
    ): Call<ResponseBody>
}
