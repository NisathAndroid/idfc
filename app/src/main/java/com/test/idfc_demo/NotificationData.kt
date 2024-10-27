package com.test.idfc_demo

data class ApiResponse(
    val data: NotificationData,
    val token: String
)

data class NotificationData(
    val body: String,
    val customername: String,
    val lat: String,
    val location: String,
    val lon: String,
    val title: String
)