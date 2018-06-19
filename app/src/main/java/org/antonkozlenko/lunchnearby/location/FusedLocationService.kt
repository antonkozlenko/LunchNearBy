package org.antonkozlenko.lunchnearby.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices

class FusedLocationService(context: Context): LocationService {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override fun getLastLocation(onSuccess: (location: Location) -> Unit, onError: (error: String) -> Unit) {
        val lastLocationTask = fusedLocationClient.lastLocation
        lastLocationTask.addOnSuccessListener(onSuccess)
        lastLocationTask.addOnFailureListener({
            onError(it.message ?: "Unknown error")
        })
    }
}