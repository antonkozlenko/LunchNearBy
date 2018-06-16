package org.antonkozlenko.lunchnearby.data

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.util.Log
import org.antonkozlenko.lunchnearby.api.GooglePlacesService
import org.antonkozlenko.lunchnearby.api.PlacesSortCriteria
import org.antonkozlenko.lunchnearby.model.LocationData
import org.antonkozlenko.lunchnearby.model.RestaurantSearchResult
import java.util.concurrent.Executors

class GooglePlacesRepository(val apiService: GooglePlacesService) {
    private val TAG = "GooglePlacesRepository"

//    fun searchRestaurantsWithRadius(location: LocationData,
//                                radius: Int,
//                                keyword: String) : RestaurantSearchResult {
//        Log.d(TAG, "New search: $keyword, location: $location, radius: $radius")
//
//        val restaurants = MutableLiveData<List<Restaurant>>()
//        val networkErrors = MutableLiveData<String>()
//
//        searchNearByRestaurantsWithRadius(apiService, location, radius, keyword, {data ->
//            restaurants.postValue(data.map {
//                val locationData = LocationData(it.geometry.location)
//                return@map Restaurant(it.place_id, it.name, it.vicinity, it.icon,
//                        locationData, it.rating)
//            })
//        }, {error ->
//            networkErrors.postValue(error)
//        })
//        return RestaurantSearchResult(restaurants, networkErrors)
//    }

    fun searchRestaurants(location: LocationData,
                          sortCriteria: PlacesSortCriteria,
                          keyword: String) : RestaurantSearchResult {
        Log.d(TAG, "New search: location: $location, sortCriteria: ${sortCriteria.criteria}, " +
                "keyword: $keyword")

        val errors = MutableLiveData<String>()

        val sourceFactory = GooglePlacesDataSourceFactory(apiService, location, sortCriteria, keyword, errors)

        val executor = Executors.newSingleThreadExecutor()

        val pagedListConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(NETWORK_PAGE_SIZE)
                .build()

        val livePagedList = LivePagedListBuilder(sourceFactory, pagedListConfig)
                .setFetchExecutor(executor)
                .build()

        return RestaurantSearchResult(livePagedList, errors)
    }

    companion object {
        private const val NETWORK_PAGE_SIZE = 20
    }
}
