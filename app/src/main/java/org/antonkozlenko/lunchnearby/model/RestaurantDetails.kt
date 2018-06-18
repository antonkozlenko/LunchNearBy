package org.antonkozlenko.lunchnearby.model


data class RestaurantDetails(
        val id: String,
        val name: String,
        val address: String,
        val icon: String,
        val location: LocationData,
        val rating: Float,
        val formattedPhoneNumber: String?,
        val internationalPhoneNumber: String?,
        val website: String?
)