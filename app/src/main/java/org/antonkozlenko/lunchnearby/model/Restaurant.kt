package org.antonkozlenko.lunchnearby.model


data class Restaurant(
        val id: String,
        val name: String,
        val address: String,
        val icon: String,
        val location: LocationData,
        val rating: Float
)