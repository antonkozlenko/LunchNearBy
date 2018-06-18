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
import kotlinx.android.synthetic.main.activity_search_restaurants.*
import org.antonkozlenko.lunchnearby.data.NetworkState
import org.antonkozlenko.lunchnearby.model.Restaurant


class SearchRestaurantsActivity : AppCompatActivity() {

    private lateinit var viewModel: SearchRestaurantsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_restaurants)

        // get the view model
        viewModel = ViewModelProviders.of(this, Injection.provideAppViewModelFactory(this))
                .get(SearchRestaurantsViewModel::class.java)

        // add dividers between RecyclerView's row items
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        restaurants_list.addItemDecoration(decoration)

        initAdapter()
        initSwipeToRefresh()
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
            adapter.submitList(it)
        })
        viewModel.networkState.observe(this, Observer<NetworkState> {
            Toast.makeText(this, "Status ${it?.status}", Toast.LENGTH_SHORT).show()
        })
    }

    private fun initSwipeToRefresh() {
        viewModel.refreshState.observe(this, Observer {
            restaurants_refresh.isRefreshing = it == NetworkState.LOADING
        })
        restaurants_refresh.setOnRefreshListener {
            viewModel.refresh()
        }
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

    companion object {
        private const val LAST_SEARCH_QUERY: String = "last_search_query"
        private const val DEFAULT_QUERY = "Pizza"
    }
}
