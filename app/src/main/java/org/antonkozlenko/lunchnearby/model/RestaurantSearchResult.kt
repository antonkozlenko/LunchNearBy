package org.antonkozlenko.lunchnearby.model

import android.arch.lifecycle.LiveData
import android.arch.paging.PagedList


data class RestaurantSearchResult(
        val data: LiveData<PagedList<Restaurant>>,
        val networkErrors: LiveData<String>
)