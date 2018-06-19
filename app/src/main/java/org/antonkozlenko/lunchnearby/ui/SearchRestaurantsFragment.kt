package org.antonkozlenko.lunchnearby.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import org.antonkozlenko.lunchnearby.R
import org.antonkozlenko.lunchnearby.Injection
import kotlinx.android.synthetic.main.activity_search_restaurants.*
import org.antonkozlenko.lunchnearby.data.NetworkState
import org.antonkozlenko.lunchnearby.model.Restaurant
import org.antonkozlenko.lunchnearby.model.RestaurantDetails


class SearchRestaurantsFragment : Fragment() {

    private lateinit var viewModel: SearchRestaurantsViewModel

    private lateinit var itemSelectionListener: OnRestaurantSelectionListener

    private lateinit var restaurants_list: RecyclerView
    private lateinit var restaurants_refresh: SwipeRefreshLayout
    private lateinit var search_restaurant: EditText

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        // get the view model
        viewModel = ViewModelProviders.of(this, Injection.provideAppViewModelFactory(activity!!))
                .get(SearchRestaurantsViewModel::class.java)

        if (context is OnRestaurantSelectionListener) {
            itemSelectionListener = context
        } else {
            throw IllegalStateException("Activity must implement selection listener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.activity_search_restaurants, container, false)

        restaurants_list = layout.findViewById(R.id.restaurants_list)
        restaurants_refresh = layout.findViewById(R.id.restaurants_refresh)
        search_restaurant = layout.findViewById(R.id.search_restaurant)

        // add dividers between RecyclerView's row items
        val decoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        restaurants_list.addItemDecoration(decoration)

        initAdapter()
        initSwipeToRefresh()
        val query = savedInstanceState?.getString(LAST_SEARCH_QUERY) ?: DEFAULT_QUERY
        viewModel.searchRestaurants(query)
        initSearch(query)
        return layout
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(LAST_SEARCH_QUERY, viewModel.lastQueryValue())
    }

    private fun initAdapter() {
        val adapter = RestaurantsAdapter(itemSelectionListener)
        restaurants_list.adapter = adapter
        viewModel.restaurants.observe(this, Observer<PagedList<Restaurant>> {
            Log.d("Activity", "list: ${it?.size}")
            adapter.submitList(it)
        })
        viewModel.networkState.observe(this, Observer<NetworkState> {
            Toast.makeText(activity, "Status ${it?.status}", Toast.LENGTH_SHORT).show()
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

    interface OnRestaurantSelectionListener {
        fun onRestaurantSelected(restaurant: Restaurant)
    }

    companion object {
        private const val LAST_SEARCH_QUERY: String = "last_search_query"
        private const val DEFAULT_QUERY = "Pizza"
    }
}
