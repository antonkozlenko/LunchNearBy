package org.antonkozlenko.lunchnearby.model

import android.location.Location
import org.antonkozlenko.lunchnearby.api.LocationDataResponse


data class LocationData(
        val latitude: Double,
        val longtitude: Double
) {

    constructor(dataResponse: LocationDataResponse) : this(dataResponse.lat, dataResponse.lng)

    constructor(location: Location) : this(location.latitude, location.longitude)

    override fun toString(): String {
        return "$latitude,$longtitude"
    }
}