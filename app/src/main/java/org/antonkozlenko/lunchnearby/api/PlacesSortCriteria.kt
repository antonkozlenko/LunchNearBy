package org.antonkozlenko.lunchnearby.api

enum class SortCriteria {
    BEST_MATCH,
    DISTANCE
}

private val MAX_RADIUS = 50_000

@Suppress("DataClassPrivateConstructor")
data class PlacesSortCriteria private constructor(
        val criteria: SortCriteria,
        val queryValue: String?,
        var radius: Int? = null) {
    companion object {
        val BEST_MATCH = PlacesSortCriteria(SortCriteria.BEST_MATCH, "prominence", MAX_RADIUS)
        val DISTANCE = PlacesSortCriteria(SortCriteria.DISTANCE, "distance")
    }
}