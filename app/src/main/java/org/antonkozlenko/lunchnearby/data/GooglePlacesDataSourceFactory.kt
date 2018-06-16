package org.antonkozlenko.lunchnearby.data

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource
import org.antonkozlenko.lunchnearby.api.GooglePlacesService
import org.antonkozlenko.lunchnearby.api.PlacesSortCriteria
import org.antonkozlenko.lunchnearby.model.LocationData
import org.antonkozlenko.lunchnearby.model.Restaurant


class GooglePlacesDataSourceFactory(
        private val apiService: GooglePlacesService,
        private val location: LocationData,
        private val sortCriteria: PlacesSortCriteria,
        private val keyword: String,
        private val errors: MutableLiveData<String>) : DataSource.Factory<String, Restaurant>() {

    val sourceLiveData = MutableLiveData<GooglePlacesDataSource>()

    override fun create(): DataSource<String, Restaurant> {
        val source = GooglePlacesDataSource(apiService, location, sortCriteria, keyword, errors)
        sourceLiveData.postValue(source)
        return source
    }
}