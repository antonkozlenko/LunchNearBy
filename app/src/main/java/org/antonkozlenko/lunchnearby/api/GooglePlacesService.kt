package org.antonkozlenko.lunchnearby.api

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import org.antonkozlenko.lunchnearby.BuildConfig
import org.antonkozlenko.lunchnearby.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val TAG = "GooglePlacesService"

/**
 * Search restaurants based on a query.
 * Trigger a request to the Google Places API with the following params:
 * @param location current location
 * @param keyword restaurant keyword
 * @param sortCriteria sorting order
 * @param pageToken page token for next results
 *
 * The result of the request is handled by the implementation of the functions passed as params
 * @param onSuccess function that defines how to handle the list of repos received
 * @param onError function that defines how to handle request failure
 */
fun searchNearByRestaurants(
        service: GooglePlacesService,
        location: LocationData,
        sortCriteria: PlacesSortCriteria,
        keyword: String,
        pageToken: String? = null,
        onSuccess: (searchResponse: RestaurantSearchDataResponse) -> Unit,
        onError: (error: String) -> Unit) {
    Log.d(TAG, "location: $location, sortCriteria: ${sortCriteria.criteria}, " +
            "keyword: $keyword, pageToken: $pageToken")

    service.searchNearByRestaurants(location.toString(), sortCriteria.queryValue, sortCriteria.radius,
            keyword, pageToken).enqueue(
            object : Callback<RestaurantSearchDataResponse> {
                override fun onFailure(call: Call<RestaurantSearchDataResponse>?, t: Throwable) {
                    Log.d(TAG, "fail to get data")
                    onError(t.message ?: "unknown error")
                }

                override fun onResponse(
                        call: Call<RestaurantSearchDataResponse>?,
                        response: Response<RestaurantSearchDataResponse>
                ) {
                    Log.d(TAG, "got a response $response")
                    if (response.isSuccessful) {
                        val searchResponse: RestaurantSearchDataResponse? = response.body()
                        Log.d(TAG, "Search status: ${searchResponse?.status}")

                        searchResponse?.let {
                            if (searchResponse.status.equals("OK")) {
                                onSuccess.invoke(searchResponse)
                            } else {
                                onError("Wrong response status is ${searchResponse.status}")
                            }
                        } ?: onError("Response body is NULL")
                    } else {
                        onError(response.errorBody()?.string() ?: "Unknown error")
                    }
                }
            }
    )
}

/**
 * Get place detailed information
 * Trigger a request to the Google Places API with the following params:
 * @param placeId place ID
 *
 * The result of the request is handled by the implementation of the functions passed as params
 * @param onSuccess function that defines how to handle the list of repos received
 * @param onError function that defines how to handle request failure
 */
fun getGooglePlaceDetails(
        service: GooglePlacesService,
        placeId: String,
        onSuccess: (detailsResponse: RestaurantDetailsResponseData) -> Unit,
        onError: (error: String) -> Unit) {
    Log.d(TAG, "Details for placeId: $placeId")

    service.getPlaceDetails(placeId).enqueue(
            object : Callback<RestaurantDetailsResponseData> {
                override fun onFailure(call: Call<RestaurantDetailsResponseData>?, t: Throwable) {
                    Log.d(TAG, "fail to get data")
                    onError(t.message ?: "unknown error")
                }

                override fun onResponse(
                        call: Call<RestaurantDetailsResponseData>?,
                        response: Response<RestaurantDetailsResponseData>
                ) {
                    Log.d(TAG, "got a response $response")
                    if (response.isSuccessful) {
                        val detailsResponse: RestaurantDetailsResponseData? = response.body()
                        detailsResponse?.let(onSuccess) ?: onError("Response body is NULL")
                    } else {
                        onError(response.errorBody()?.string() ?: "Unknown error")
                    }
                }
            }
    )
}

/**
 * Google Places API communication setup via Retrofit.
 */
interface GooglePlacesService {

    /**
     * Get nearby places by order.
     */
    @GET("nearbysearch/json?type=restaurant&key=${BuildConfig.API_KEY}")
    fun searchNearByRestaurants(
            @Query("location") location: String,
            @Query("rankby") rankBy: String?,
            @Query("radius") radius: Int?,
            @Query("keyword") keyword: String,
            @Query("pagetoken") pageToken: String? = null): Call<RestaurantSearchDataResponse>

    /**
     * Get place details
     */
    @GET("details/json?key=${BuildConfig.API_KEY}")
    fun getPlaceDetails(
            @Query("placeid") placeId: String): Call<RestaurantDetailsResponseData>

    companion object {
        private const val BASE_URL = "https://maps.googleapis.com/maps/api/place/"

        fun create(): GooglePlacesService {
            val logger = HttpLoggingInterceptor()
            logger.level = Level.BASIC

            val client = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .build()
            return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(GooglePlacesService::class.java)
        }
    }

}