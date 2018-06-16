package org.antonkozlenko.lunchnearby.api

data class RestaurantSearchDataResponse(
        val results: List<RestaurantDataResponse>,
        val next_page_token: String? = null,
        val status: String
)

data class RestaurantDataResponse(
        val place_id: String,
        val name: String,
        val vicinity: String,
        val icon: String,
        val geometry: GeometryResponse,
        val rating: Float
)

data class GeometryResponse(
        val location: LocationDataResponse,
        val viewport: ViewPortDataResponse
)

data class LocationDataResponse(
        val lat: Double,
        val lng: Double
)

data class ViewPortDataResponse(
        val northeast: LocationDataResponse,
        val southwest: LocationDataResponse
)