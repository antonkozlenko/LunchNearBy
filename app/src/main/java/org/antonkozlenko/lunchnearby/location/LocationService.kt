package org.antonkozlenko.lunchnearby.location

import android.location.Location
import android.util.Log

private const val TAG = "LocationService"

fun getLastLocation(service: LocationService,
                    onSuccess: (location: Location) -> Unit,
                    onError: (error: String) -> Unit) {
    Log.d(TAG, "Request last location")
    service.getLastLocation(onSuccess, onError)
}

interface LocationService {

    fun getLastLocation(
            onSuccess: (location: Location) -> Unit,
            onError: (error: String) -> Unit
    )
}