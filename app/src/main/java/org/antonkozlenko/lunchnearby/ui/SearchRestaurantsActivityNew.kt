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

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import org.antonkozlenko.lunchnearby.R
import org.antonkozlenko.lunchnearby.Injection
import kotlinx.android.synthetic.main.activity_search_repositories.*
import kotlinx.android.synthetic.main.activity_search_restaurants.*
import org.antonkozlenko.lunchnearby.data.NetworkState
import org.antonkozlenko.lunchnearby.model.Restaurant


class SearchRestaurantsActivityNew : AppCompatActivity() {

    private lateinit var viewModel: SearchRestaurantsViewModelNew

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_restaurants)

        // get the view model
        viewModel = ViewModelProviders.of(this, Injection.provideAppViewModelFactoryNew(this))
                .get(SearchRestaurantsViewModelNew::class.java)

        // add dividers between RecyclerView's row items
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        restaurants_list.addItemDecoration(decoration)

        initAdapter()
        val query = savedInstanceState?.getString(LAST_SEARCH_QUERY) ?: DEFAULT_QUERY
        viewModel.searchRestaurants(query)
        initSearch(query)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(LAST_SEARCH_QUERY, viewModel.lastQueryValue())
    }

    private fun initAdapter() {
        val adapter = RestaurantsAdapter()
        restaurants_list.adapter = adapter
        viewModel.restaurants.observe(this, Observer<PagedList<Restaurant>> {
            Log.d("Activity", "list: ${it?.size}")
//            showEmptyList(it?.size == 0)
            adapter.submitList(it)
        })
        viewModel.networkState.observe(this, Observer<NetworkState> {
            Toast.makeText(this, "Status ${it?.status}", Toast.LENGTH_SHORT).show()
        })

        globalRestaurants.observe(this, Observer<List<Restaurant>> {
            Log.d("Activity", "list: ${it?.size}")
            Toast.makeText(this, "List: ${it}", Toast.LENGTH_SHORT).show()
        })
    }

    private fun initSearch(query: String) {
        search_restaurant.setText(query)

        search_restaurant.setOnEditorActionListener({ _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                updateRestaurantsListFromInput()
                true
            } else {
                false
            }
        })
        search_restaurant.setOnKeyListener({ _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updateRestaurantsListFromInput()
                true
            } else {
                false
            }
        })

        // Sidney, Australia
//        val location = LocationData(-33.8670522, 151.1957362)
//
//        val lifecycle = this
//
//        async {
//
//            Log.d("PlacesAPI", "Search with sorting")
//            val resp = placesRepo.searchRestaurants(location, PlacesSortCriteria.DISTANCE, "pizza")
//            resp.data.observe(lifecycle, Observer { list: List<Restaurant>? ->
//                Log.d("PlacesAPI", "List: ${list.toString()}")
//            })
//        }
    }

    private fun updateRestaurantsListFromInput() {
        search_restaurant.text.trim().let {
            if (it.isNotEmpty()) {
                restaurants_list.scrollToPosition(0)
                viewModel.searchRestaurants(it.toString())
                (restaurants_list.adapter as? RestaurantsAdapter)?.submitList(null)
            }
        }
    }

    private fun showEmptyList(show: Boolean) {
        if (show) {
            restaurants_emptyList.visibility = View.VISIBLE
            restaurants_list.visibility = View.GONE
        } else {
            restaurants_emptyList.visibility = View.GONE
            restaurants_list.visibility = View.VISIBLE
        }
    }

    companion object {
        private const val LAST_SEARCH_QUERY: String = "last_search_query"
        private const val DEFAULT_QUERY = "Pizza"
    }
}
