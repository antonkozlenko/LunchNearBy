package org.antonkozlenko.lunchnearby.ui

import android.arch.lifecycle.*
import org.antonkozlenko.lunchnearby.api.PlacesSortCriteria
import org.antonkozlenko.lunchnearby.data.GooglePlacesRepository
import org.antonkozlenko.lunchnearby.model.LocationData
import org.antonkozlenko.lunchnearby.model.RestaurantDetails
import org.antonkozlenko.lunchnearby.model.RestaurantSearchResult

/**
 * ViewModel for the [SearchRestaurantsActivity] screen.
 * The ViewModel works with the [GooglePlacesRepository] to get the data.
 */

class SearchRestaurantsViewModel(private val repository: GooglePlacesRepository) : ViewModel() {
    // Sidney, Australia
    private val STUB_LOCATION = LocationData(-33.8670522, 151.1957362)

    private val queryLiveData = MutableLiveData<String>()
    private val locationData = MutableLiveData<LocationData>()
    private val sortCriteria = MutableLiveData<PlacesSortCriteria>()
    private val placeIdData = MutableLiveData<String>()

    private val restaurantsResult = MediatorLiveData<RestaurantSearchResult>().apply {
        // Listen for query changes
        addSource(queryLiveData) {
            value = repository.searchRestaurants(
                    lastLocationValue(),
                    lastSortCriteriaValue(),
                    it ?: lastQueryValue())
        }
        // Listen for location changes
        addSource(locationData) {
            value = repository.searchRestaurants(
                    lastLocationValue(),
                    lastSortCriteriaValue(),
                    lastQueryValue())
        }
        // Listen for sorting changes
        addSource(sortCriteria) {
            value = repository.searchRestaurants(
                    lastLocationValue(),
                    it ?: lastSortCriteriaValue(),
                    lastQueryValue())
        }
    }

    val restaurants = Transformations.switchMap(restaurantsResult) { it.data }!!
    val networkState = Transformations.switchMap(restaurantsResult) { it.networkState }!!
    val refreshState = Transformations.switchMap(restaurantsResult) { it.refreshState }!!

    private val detailsResult = Transformations.map(placeIdData) {
        repository.getPlaceDetailedInfo(it)
    }

    val restaurantDetails = Transformations.switchMap(detailsResult) { it.data }!!

    fun refresh() {
        restaurantsResult.value?.refresh?.invoke()
    }

    fun retry() {
        val listing = restaurantsResult.value
        listing?.retry?.invoke()
    }

    /**
     * Search a restaurants based on a query string.
     */
    fun searchRestaurants(queryString: String) {
        queryLiveData.postValue(queryString)
    }

    fun setSortingCriteria(sortBy: PlacesSortCriteria) {
        sortCriteria.postValue(sortBy)
    }

    fun getRestaurantDetails(id: String) {
        placeIdData.postValue(id)
    }

    /**
     * Get the last query value.
     */
    fun lastQueryValue(): String = queryLiveData.value ?: "Pizza"

    /**
     * Get the last location value.
     */
    fun lastLocationValue(): LocationData = locationData.value ?: STUB_LOCATION

    /**
     * Get the last sorting value.
     */
    fun lastSortCriteriaValue(): PlacesSortCriteria = sortCriteria.value ?: PlacesSortCriteria.BEST_MATCH

    fun lastPlaceDetails() : RestaurantDetails? = restaurantDetails.value
}