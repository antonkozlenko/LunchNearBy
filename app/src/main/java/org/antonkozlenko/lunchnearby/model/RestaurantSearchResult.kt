package org.antonkozlenko.lunchnearby.model

import android.arch.lifecycle.LiveData
import android.arch.paging.PagedList

import org.antonkozlenko.lunchnearby.data.NetworkState

data class RestaurantSearchResult(
        val data: LiveData<PagedList<Restaurant>>,
        // represents the network request status to show to the user
        val networkState: LiveData<NetworkState>,
        // represents the refresh status to show to the user. Separate from networkState, this
        // value is importantly only when refresh is requested.
        val refreshState: LiveData<NetworkState>,
        // refreshes the whole data and fetches it from scratch.
        val refresh: () -> Unit,
        // retries any failed requests.
        val retry: () -> Unit
)