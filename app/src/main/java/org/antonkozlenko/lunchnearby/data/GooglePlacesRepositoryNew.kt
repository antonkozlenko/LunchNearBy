package org.antonkozlenko.lunchnearby.data

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.util.Log
import org.antonkozlenko.lunchnearby.api.GooglePlacesService
import org.antonkozlenko.lunchnearby.api.PlacesSortCriteria
import org.antonkozlenko.lunchnearby.model.LocationData
import org.antonkozlenko.lunchnearby.model.Restaurant
import org.antonkozlenko.lunchnearby.model.RestaurantSearchResult
import org.antonkozlenko.lunchnearby.model.RestaurantSearchResultNew
import java.util.concurrent.Executors

class GooglePlacesRepositoryNew(val apiService: GooglePlacesService) {
    private val TAG = "GooglePlacesRepository"

    fun searchRestaurants(location: LocationData,
                          sortCriteria: PlacesSortCriteria,
                          keyword: String) : RestaurantSearchResultNew {
        Log.d(TAG, "New search: location: $location, sortCriteria: ${sortCriteria.criteria}, " +
                "keyword: $keyword")

        val executor = Executors.newSingleThreadExecutor()

        val sourceFactory = GooglePlacesDataSourceFactoryNew(apiService, location, sortCriteria, keyword, executor)

        val livePagedList = LivePagedListBuilder(sourceFactory, NETWORK_PAGE_SIZE)
                .setFetchExecutor(executor)
                .build()

        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }

        return RestaurantSearchResultNew(
                livePagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData, {
                    it.networkState
                }),
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState
        )
    }

    companion object {
        private const val NETWORK_PAGE_SIZE = 20
    }
}
