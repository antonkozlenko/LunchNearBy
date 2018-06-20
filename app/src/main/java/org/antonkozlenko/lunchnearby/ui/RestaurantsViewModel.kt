package org.antonkozlenko.lunchnearby.ui

import android.arch.lifecycle.*
import org.antonkozlenko.lunchnearby.api.PlacesSortCriteria
import org.antonkozlenko.lunchnearby.data.GooglePlacesRepository
import org.antonkozlenko.lunchnearby.data.NetworkState
import org.antonkozlenko.lunchnearby.location.LocationService
import org.antonkozlenko.lunchnearby.location.getLastLocation
import org.antonkozlenko.lunchnearby.model.LocationData
import org.antonkozlenko.lunchnearby.model.RestaurantSearchResult

/**
 * ViewModel for the [SearchRestaurantsActivity] screen.
 * The ViewModel works with the [GooglePlacesRepository] to get the data.
 */

class RestaurantsViewModel(private val repository: GooglePlacesRepository,
                           private val locationService: LocationService) : ViewModel() {
    // Sidney, Australia
    private val STUB_LOCATION = LocationData(-33.8670522, 151.1957362)

    val locationPermissionGranted = MutableLiveData<Boolean>()

    private val locationUpdateRequested = MutableLiveData<Boolean>()

    private val locationData = MediatorLiveData<LocationData>().apply {
        addSource(locationUpdateRequested) {
            it?.let {
                if (it) {
                    getLastLocation(locationService, onSuccess = {
                        value = it?.let { LocationData(it) } ?: STUB_LOCATION
                    }, onError = {
                        value = STUB_LOCATION
                    })
                    locationUpdateRequested.postValue(false)
                }
            }
        }
    }


    private val queryLiveData = MutableLiveData<String>()
    private val sortCriteria = MutableLiveData<PlacesSortCriteria>()
    private val placeIdData = MutableLiveData<String>()

    private val restaurantsResult = MediatorLiveData<RestaurantSearchResult>().apply {
        // Listen for query changes
        addSource(queryLiveData) {
            locationUpdateRequested.postValue(true)
        }

        // Listen for sorting changes
        addSource(sortCriteria) {
            locationUpdateRequested.postValue(true)
        }

        // Listen for location changes
        addSource(locationData) {
            value = repository.searchRestaurants(
                    it!!,
                    lastSortCriteriaValue(),
                    lastQueryValue())
        }
    }

    private val detailsResult = Transformations.map(placeIdData) {
        repository.getPlaceDetailedInfo(it)
    }

    val restaurants = Transformations.switchMap(restaurantsResult) { it.data }!!

    val restaurantDetails = Transformations.switchMap(detailsResult) { it.data }!!

    val networkState = MediatorLiveData<NetworkState>().apply {
        addSource(Transformations.switchMap(restaurantsResult) {it.networkState}) {
            value = it
        }

        addSource(Transformations.switchMap(detailsResult) {it.networkState}) {
            value = it
        }
    }

    val refreshState = Transformations.switchMap(restaurantsResult) { it.refreshState }!!

    fun refresh() {
        locationUpdateRequested.postValue(true)
    }

    fun retry() {
        val listing = restaurantsResult.value
        listing?.retry?.invoke()
    }

    fun setLocationPermissionGranted(granted: Boolean) {
        locationPermissionGranted.postValue(granted)
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

}