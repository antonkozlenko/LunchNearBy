package org.antonkozlenko.lunchnearby.ui

import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.antonkozlenko.lunchnearby.R
import org.antonkozlenko.lunchnearby.model.Restaurant

/**
 * View Holder for a [Restaurant] RecyclerView list item.
 */
class RestaurantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val name: TextView = view.findViewById(R.id.restaurant_name)
    private val address: TextView = view.findViewById(R.id.restaurant_address)
    private val icon: ImageView = view.findViewById(R.id.restaurant_icon)
    private val rating: TextView = view.findViewById(R.id.restaurant_rating)

    private var restaurant: Restaurant? = null

    init {
        view.setOnClickListener {
            restaurant?.icon?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                view.context.startActivity(intent)
            }
        }
    }

    fun bind(restaurant: Restaurant?) {
        if (restaurant == null) {
            val resources = itemView.resources
            name.text = resources.getString(R.string.loading)
            rating.text = resources.getString(R.string.unknown)
        } else {
            showRestaurantData(restaurant)
        }
    }

    private fun showRestaurantData(restaurant: Restaurant) {
        this.restaurant = restaurant
        name.text = restaurant.name

        address.text = restaurant.address
        rating.text = restaurant.rating.toString()

        // TODO implement displaying icon
    }

    companion object {
        fun create(parent: ViewGroup): RestaurantViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.restaurant_view_item, parent, false)
            return RestaurantViewHolder(view)
        }
    }
}