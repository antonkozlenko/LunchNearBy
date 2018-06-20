package org.antonkozlenko.lunchnearby.ui

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.antonkozlenko.lunchnearby.data.GooglePlacesRepository
import org.antonkozlenko.lunchnearby.location.LocationService

/**
 * Factory for ViewModels
 */
class AppViewModelFactory(private val repository: GooglePlacesRepository,
                          private val locationService: LocationService) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RestaurantsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RestaurantsViewModel(repository, locationService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}