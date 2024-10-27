package com.test.idfc_demo

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://us-central1-seeforme-server-function.cloudfunctions.net/"  // Use 10.0.2.2 for localhost in Android Emulator

    val api: NotificationAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NotificationAPI::class.java)
    }
}