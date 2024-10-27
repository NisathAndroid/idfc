package com.test.idfc_demo

import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.util.Locale

object GetAddress {

    fun getAddressFromLatLng(context: Context, latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address: Address = addresses[0]
                address.getAddressLine(0) ?: "Address not found"
            } else {
                "Address not found"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Unable to get address"
        }
    }
}