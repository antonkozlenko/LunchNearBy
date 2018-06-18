package org.antonkozlenko.lunchnearby.data

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource
import org.antonkozlenko.lunchnearby.api.GooglePlacesService
import org.antonkozlenko.lunchnearby.api.PlacesSortCriteria
import org.antonkozlenko.lunchnearby.model.LocationData
import org.antonkozlenko.lunchnearby.model.Restaurant
import java.util.concurrent.Executor


class GooglePlacesDataSourceFactoryNew(
        private val apiService: GooglePlacesService,
        private val location: LocationData,
        private val sortCriteria: PlacesSortCriteria,
        private val keyword: String,
        private val retryExecutor: Executor) : DataSource.Factory<String, Restaurant>() {

    val sourceLiveData = MutableLiveData<GooglePlacesDataSourceNew>()

    override fun create(): DataSource<String, Restaurant> {
        val source = GooglePlacesDataSourceNew(apiService, location, sortCriteria, keyword, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}