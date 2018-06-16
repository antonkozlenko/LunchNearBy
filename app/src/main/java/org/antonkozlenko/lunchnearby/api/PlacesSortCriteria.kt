package org.antonkozlenko.lunchnearby.api

enum class SortCriteria {
    BEST_MATCH,
    DISTANCE
}

@Suppress("DataClassPrivateConstructor")
data class PlacesSortCriteria private constructor(
        val criteria: SortCriteria,
        val queryValue: String?) {
    companion object {
        val BEST_MATCH = PlacesSortCriteria(SortCriteria.BEST_MATCH, null)
        val DISTANCE = PlacesSortCriteria(SortCriteria.DISTANCE, "distance")
    }
}