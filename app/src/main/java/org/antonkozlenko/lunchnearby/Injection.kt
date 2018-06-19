package org.antonkozlenko.lunchnearby

import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import org.antonkozlenko.lunchnearby.api.GooglePlacesService
import org.antonkozlenko.lunchnearby.data.GooglePlacesRepository
import org.antonkozlenko.lunchnearby.location.FusedLocationService
import org.antonkozlenko.lunchnearby.location.LocationService
import org.antonkozlenko.lunchnearby.ui.AppViewModelFactory

/**
 * Class that handles object creation.
 * Like this, objects can be passed as parameters in the constructors and then replaced for
 * testing, where needed.
 */
object Injection {

    fun provideLocationService(context: Context): LocationService {
        return FusedLocationService(context)
    }

    fun provideGooglePlacesRepository(context: Context): GooglePlacesRepository {
        return GooglePlacesRepository(GooglePlacesService.create())
    }

    fun provideAppViewModelFactory(context: Context): ViewModelProvider.Factory {
        return AppViewModelFactory(provideGooglePlacesRepository(context),
                provideLocationService(context))
    }

}