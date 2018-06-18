package org.antonkozlenko.lunchnearby.ui

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import org.antonkozlenko.lunchnearby.data.GooglePlacesRepository

/**
 * Factory for ViewModels
 */
class AppViewModelFactory(private val repository: GooglePlacesRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchRestaurantsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchRestaurantsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}