package org.antonkozlenko.lunchnearby.model

import org.antonkozlenko.lunchnearby.api.LocationDataResponse


data class LocationData(
        val latitude: Double,
        val longtitude: Double
) {

    constructor(dataResponse: LocationDataResponse) : this(dataResponse.lat, dataResponse.lng)

    override fun toString(): String {
        return "$latitude,$longtitude"
    }
}