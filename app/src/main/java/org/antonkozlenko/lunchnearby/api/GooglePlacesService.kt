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
 * Search repos based on a query.
 * Trigger a request to the Github searchRepo API with the following params:
 * @param query searchRepo keyword
 * @param page request page index
 * @param itemsPerPage number of repositories to be returned by the Github API per page
 *
 * The result of the request is handled by the implementation of the functions passed as params
 * @param onSuccess function that defines how to handle the list of repos received
 * @param onError function that defines how to handle request failure
 */
fun searchNearByRestaurantsWithRadius(
        service: GooglePlacesService,
        location: LocationData,
        radius: Int,
        keyword: String,
        onSuccess: (repos: List<RestaurantDataResponse>) -> Unit,
        onError: (error: String) -> Unit) {
    Log.d(TAG, "location: $location, radius: $radius, keyword: $keyword")

    service.searchNearByRestaurantsWithRadius(location.toString(), radius, keyword).enqueue(
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
                        val restaurants = response.body()?.results ?: emptyList()
                        onSuccess(restaurants)
                    } else {
                        onError(response.errorBody()?.string() ?: "Unknown error")
                    }
                }
            }
    )
}

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

    service.searchNearByRestaurants(location.toString(), sortCriteria.queryValue, keyword, pageToken).enqueue(
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
                        searchResponse?.let(onSuccess) ?: onError("Response body is NULL")
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
     * Get nearby places.
     */
    @GET("nearbysearch/json?type=restaurant&key=${BuildConfig.API_KEY}")
    fun searchNearByRestaurantsWithRadius(
            @Query("location") location: String,
            @Query("radius") radius: Int,
            @Query("keyword") keyword: String): Call<RestaurantSearchDataResponse>


    /**
     * Get nearby places by order.
     */
    @GET("nearbysearch/json?type=restaurant&key=${BuildConfig.API_KEY}")
    fun searchNearByRestaurants(
            @Query("location") location: String,
            @Query("rankby") rankBy: String?,
            @Query("keyword") keyword: String,
            @Query("pagetoken") nextPageToken: String? = null): Call<RestaurantSearchDataResponse>

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