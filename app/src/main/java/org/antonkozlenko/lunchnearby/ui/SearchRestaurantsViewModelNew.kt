/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.antonkozlenko.lunchnearby.ui

import android.arch.lifecycle.*
import android.util.Log
import org.antonkozlenko.lunchnearby.api.PlacesSortCriteria
import org.antonkozlenko.lunchnearby.data.GooglePlacesRepository
import org.antonkozlenko.lunchnearby.data.GooglePlacesRepositoryNew
import org.antonkozlenko.lunchnearby.model.LocationData
import org.antonkozlenko.lunchnearby.model.Restaurant
import org.antonkozlenko.lunchnearby.model.RestaurantSearchResult
import org.antonkozlenko.lunchnearby.model.RestaurantSearchResultNew

/**
 * ViewModel for the [SearchRepositoriesActivity] screen.
 * The ViewModel works with the [GithubRepository] to get the data.
 */

class SearchRestaurantsViewModelNew(private val repository: GooglePlacesRepositoryNew) : ViewModel() {
    // Sidney, Australia
    private val STUB_LOCATION = LocationData(-33.8670522, 151.1957362)

    private val queryLiveData = MutableLiveData<String>()
    private val locationData = MutableLiveData<LocationData>()
    private val sortCriteria = MutableLiveData<PlacesSortCriteria>()

    private val restaurantsResult = MediatorLiveData<RestaurantSearchResult>().apply {
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