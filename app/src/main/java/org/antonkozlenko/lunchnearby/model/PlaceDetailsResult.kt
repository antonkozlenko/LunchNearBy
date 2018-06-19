package org.antonkozlenko.lunchnearby.model

import android.arch.lifecycle.LiveData
import org.antonkozlenko.lunchnearby.data.NetworkState

data class PlaceDetailsResult(
        val data: LiveData<RestaurantDetails>,
        // represents the network request status to show to the user
        val networkState: LiveData<NetworkState>
)