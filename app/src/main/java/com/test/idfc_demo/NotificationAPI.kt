package com.test.idfc_demo

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {
    @Headers("Content-Type: application/json")
    @POST("sendGpsNotification/send_gps_notification")
     fun sendNotification(
        @Body notification: ApiResponse
    ): Call<ApiResponse>
}