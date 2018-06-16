package org.antonkozlenko.lunchnearby.data

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PageKeyedDataSource
import android.util.Log
import org.antonkozlenko.lunchnearby.api.GooglePlacesService
import org.antonkozlenko.lunchnearby.api.PlacesSortCriteria
import org.antonkozlenko.lunchnearby.api.searchNearByRestaurants
import org.antonkozlenko.lunchnearby.model.LocationData
import org.antonkozlenko.lunchnearby.model.Restaurant


class GooglePlacesDataSource(
        private val apiService: GooglePlacesService,
        private val location: LocationData,
        private val sortCriteria: PlacesSortCriteria,
        private val keyword: String,
        private val errors: MutableLiveData<String>) : PageKeyedDataSource<String, Restaurant>() {


    private val TAG = "GooglePlacesDataSource"

    init {
        Log.d(TAG, "INIT called")
    }

    private val FIRST_PAGE_TOKEN = "PlaceFirstPageToken"

    private val pagesMapping: MutableMap<String, Pair<String?, String?>> = HashMap()

    override fun loadInitial(
            params: LoadInitialParams<String>,
            callback: LoadInitialCallback<String, Restaurant>) {

        Log.d(TAG, "loadInitial called")

        searchNearByRestaurants(apiService, location, sortCriteria, keyword, null, {data ->
            Log.d(TAG, "NextPageToken=" + data.next_page_token)
            pagesMapping[FIRST_PAGE_TOKEN] = Pair(null, data.next_page_token)

            val restaurants = data.results.map {
                val locationData = LocationData(it.geometry.location)
                return@map Restaurant(it.place_id, it.name, it.vicinity, it.icon,
                        locationData, it.rating)
            }

            callback.onResult(restaurants, null, data.next_page_token)
        }, {error ->
            errors.postValue(error)
        })
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, Restaurant>) {
        val currentPageToken = params.key
        Log.d(TAG, "loadAfter called, currentPage= $currentPageToken")
        pagesMapping[currentPageToken]?.second?.let {
            searchNearByRestaurants(apiService, location, sortCriteria, keyword, it, {data ->
                Log.d(TAG, "NextPageToken=" + data.next_page_token)
                pagesMapping[it] = Pair(null, data.next_page_token)
                val restaurants = data.results.map {
                    val locationData = LocationData(it.geometry.location)
                    return@map Restaurant(it.place_id, it.name, it.vicinity, it.icon,
                            locationData, it.rating)
                }

                callback.onResult(restaurants, data.next_page_token)
            }, {error ->
                errors.postValue(error)
            })
        } ?: errors.postValue("Next page token is missing, nothing to load")

    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, Restaurant>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}