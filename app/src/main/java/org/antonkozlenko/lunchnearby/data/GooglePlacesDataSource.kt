package org.antonkozlenko.lunchnearby.data

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PageKeyedDataSource
import android.util.Log
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import org.antonkozlenko.lunchnearby.api.GooglePlacesService
import org.antonkozlenko.lunchnearby.api.PlacesSortCriteria
import org.antonkozlenko.lunchnearby.api.searchNearByRestaurants
import org.antonkozlenko.lunchnearby.model.LocationData
import org.antonkozlenko.lunchnearby.model.Restaurant
import java.util.concurrent.Executor


class GooglePlacesDataSource(
        private val apiService: GooglePlacesService,
        private val location: LocationData,
        private val sortCriteria: PlacesSortCriteria,
        private val keyword: String,
        private val retryExecutor: Executor) : PageKeyedDataSource<String, Restaurant>() {


    private val TAG = "GooglePlacesDataSource"

    // keep a function reference for the retry event
    private var retry: (() -> Any)? = null

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter.
     */
    val networkState = MutableLiveData<NetworkState>()
    val initialLoad = MutableLiveData<NetworkState>()

    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let {
            retryExecutor.execute {
                it.invoke()
            }
        }
    }

    override fun loadInitial(
            params: LoadInitialParams<String>,
            callback: LoadInitialCallback<String, Restaurant>) {

        Log.d(TAG, "loadInitial called")

        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        searchNearByRestaurants(apiService, location, sortCriteria, keyword, null, {data ->
            Log.d(TAG, "NextPageToken=" + data.next_page_token)

            val restaurants = data.results.map {
                val locationData = LocationData(it.geometry.location)
                return@map Restaurant(it.place_id, it.name, it.vicinity, it.icon,
                        locationData, it.rating)
            }

            retry = null

            callback.onResult(restaurants, null, data.next_page_token)
            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)
        }, {error ->
            retry = {
                loadInitial(params, callback)
            }
            val networkError = NetworkState.error(error)
            networkState.postValue(networkError)
            initialLoad.postValue(networkError)
        })
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, Restaurant>) {
        val currentPageToken = params.key
        Log.d(TAG, "loadAfter called, currentPage= $currentPageToken")

        networkState.postValue(NetworkState.LOADING)

        currentPageToken?.let {
            async {
                // TODO check, temp solution
                delay(1000)
                searchNearByRestaurants(apiService, location, sortCriteria, keyword, it, {data ->
                    Log.d(TAG, "NextPageToken=" + data.next_page_token)
                    Log.d(TAG, "results length=" + data.results.size)
                    val restaurants = data.results.map {
                        val locationData = LocationData(it.geometry.location)
                        return@map Restaurant(it.place_id, it.name, it.vicinity, it.icon,
                                locationData, it.rating)
                    }

                    retry = null
                    callback.onResult(restaurants, data.next_page_token)
                    networkState.postValue(NetworkState.LOADED)
                }, {error ->
                    retry = {
                        loadAfter(params, callback)
                    }
                    networkState.postValue(NetworkState.error(error))
                })
            }
//            searchNearByRestaurants(apiService, location, sortCriteria, keyword, it, {data ->
//                Log.d(TAG, "NextPageToken=" + data.next_page_token)
//                Log.d(TAG, "results length=" + data.results.size)
//                val restaurants = data.results.map {
//                    val locationData = LocationData(it.geometry.location)
//                    return@map Restaurant(it.place_id, it.name, it.vicinity, it.icon,
//                            locationData, it.rating)
//                }
//
//                retry = null
//                callback.onResult(restaurants, data.next_page_token)
//                networkState.postValue(NetworkState.LOADED)
//            }, {error ->
//                retry = {
//                    loadAfter(params, callback)
//                }
//                networkState.postValue(NetworkState.error(error))
//            })
        } ?: networkState.postValue(NetworkState.error("Next page token is missing, nothing to load"))

    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, Restaurant>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}