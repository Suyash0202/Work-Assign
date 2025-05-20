package com.example.workassign.Api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiUtilities {
    private const val BASE_URL = "https://fcm.googleapis.com/v1/projects/job-assign/"

    val api: FCMApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) //
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FCMApiService::class.java)
    }
}
