package org.antonkozlenko.lunchnearby.data

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.util.Log
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.antonkozlenko.lunchnearby.api.GooglePlacesService
import org.antonkozlenko.lunchnearby.api.PlacesSortCriteria
import org.antonkozlenko.lunchnearby.api.getGooglePlaceDetails
import org.antonkozlenko.lunchnearby.model.LocationData
import org.antonkozlenko.lunchnearby.model.PlaceDetailsResult
import org.antonkozlenko.lunchnearby.model.RestaurantDetails
import org.antonkozlenko.lunchnearby.model.RestaurantSearchResult
import java.util.concurrent.Executors

class GooglePlacesRepository(val apiService: GooglePlacesService) {
    private val TAG = "GooglePlacesRepository"

    fun searchRestaurants(location: LocationData,
                          sortCriteria: PlacesSortCriteria,
                          keyword: String) : RestaurantSearchResult {
        Log.d(TAG, "New search: location: $location, sortCriteria: ${sortCriteria.criteria}, " +
                "keyword: $keyword")

        val executor = Executors.newSingleThreadExecutor()

        val sourceFactory = GooglePlacesDataSourceFactory(apiService, location, sortCriteria, keyword, executor)

        val livePagedList = LivePagedListBuilder(sourceFactory, NETWORK_PAGE_SIZE)
                .setFetchExecutor(executor)
                .build()

        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }

        return RestaurantSearchResult(
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

    fun getPlaceDetailedInfo(placeId: String) : PlaceDetailsResult {
        Log.d(TAG, "Get place details ID=$placeId")

        val placeDetails = MutableLiveData<RestaurantDetails>()
        val networkState = MutableLiveData<NetworkState>()

        launch(UI) {
            val apiCall = async(CommonPool) {
                networkState.postValue(NetworkState.LOADING)

                getGooglePlaceDetails(apiService, placeId, {data ->
                    Log.d(TAG, "Details: ${data.result}")
                    val details = data.result
                    val locationData = LocationData(details.geometry.location)
                    placeDetails.postValue(RestaurantDetails(
                            details.place_id,
                            details.name,
                            details.formatted_address,
                            details.icon,
                            locationData,
                            details.rating,
                            details.formatted_phone_number,
                            details.international_phone_number,
                            details.website))
                    networkState.postValue(NetworkState.LOADED)
                }, {error ->
                    networkState.postValue(NetworkState.error(error))
                })
            }
            apiCall.await()
        }

        return PlaceDetailsResult(placeDetails, networkState)
    }

    companion object {
        private const val NETWORK_PAGE_SIZE = 20
    }
}
