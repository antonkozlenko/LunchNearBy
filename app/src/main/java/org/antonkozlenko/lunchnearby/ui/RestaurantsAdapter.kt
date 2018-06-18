package org.antonkozlenko.lunchnearby.ui

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import org.antonkozlenko.lunchnearby.model.Restaurant

/**
 * Adapter for the list of repositories.
 */
class RestaurantsAdapter(val clickListener: (Restaurant) -> Unit) : PagedListAdapter<Restaurant, RecyclerView.ViewHolder>(RESTAURANT_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RestaurantViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val restaurantItem = getItem(position)
        if (restaurantItem != null) {
            (holder as RestaurantViewHolder).bind(restaurantItem, clickListener)
        }
    }

    companion object {
        private val RESTAURANT_COMPARATOR = object : DiffUtil.ItemCallback<Restaurant>() {
            override fun areItemsTheSame(oldItem: Restaurant, newItem: Restaurant): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Restaurant, newItem: Restaurant): Boolean =
                    oldItem == newItem
        }
    }
}