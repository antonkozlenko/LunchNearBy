package org.antonkozlenko.lunchnearby.ui

import android.arch.lifecycle.*
import org.antonkozlenko.lunchnearby.api.PlacesSortCriteria
import org.antonkozlenko.lunchnearby.data.GooglePlacesRepository
import org.antonkozlenko.lunchnearby.model.LocationData
import org.antonkozlenko.lunchnearby.model.RestaurantSearchResultNew

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

    private val restaurantsResult = MediatorLiveData<RestaurantSearchResultNew>().apply {
        // Listen for query changes
        addSource(queryLiveData, {
            val restaurantSearch = repository.searchRestaurants(
                    lastLocationValue(),
                    lastSortCriteriaValue(),
                    it ?: lastQueryValue())

//            restaurantSearch.data.observeForever({
//                Log.d("PlacesAPI", "List: ${it.toString()}")
//            })
        })
        // Listen for location changes
        addSource(locationData, {
            repository.searchRestaurants(
                    lastLocationValue(),
                    lastSortCriteriaValue(),
                    lastQueryValue())
        })
        // Listen for sorting changes
        addSource(sortCriteria, {
            repository.searchRestaurants(
                    lastLocationValue(),
                    it ?: lastSortCriteriaValue(),
                    lastQueryValue())
        })
    }

    private val simpleRestResult = Transformations.map(queryLiveData, {
        repository.searchRestaurants(
                lastLocationValue(),
                lastSortCriteriaValue(),
                it ?: lastQueryValue())
    })

    val restaurants = Transformations.switchMap(simpleRestResult, { it.data })!!
    val networkState = Transformations.switchMap(simpleRestResult, { it.networkState })!!
    val refreshState = Transformations.switchMap(simpleRestResult, { it.refreshState })!!

    fun refresh() {
        simpleRestResult.value?.refresh?.invoke()
    }

    fun retry() {
        val listing = simpleRestResult?.value
        listing?.retry?.invoke()
    }

//    val restaurants: LiveData<PagedList<Restaurant>> = Transformations.switchMap(restaurantsResult,
//            { it -> it.data })

//    val restaurants: LiveData<PagedList<Restaurant>> = Transformations.switchMap(simpleRestResult,
//            { it -> it.data })

//    val networkErrors: LiveData<String> = Transformations.switchMap(restaurantsResult,
//            { it -> it.networkErrors })

    /**
     * Search a restaurants based on a query string.
     */
    fun searchRestaurants(queryString: String) {
        queryLiveData.postValue(queryString)
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
//    fun lastSortCriteriaValue(): PlacesSortCriteria = sortCriteria.value ?: PlacesSortCriteria.BEST_MATCH
    fun lastSortCriteriaValue(): PlacesSortCriteria = sortCriteria.value ?: PlacesSortCriteria.DISTANCE

}