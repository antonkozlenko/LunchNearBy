/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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