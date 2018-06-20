package org.antonkozlenko.lunchnearby.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import org.antonkozlenko.lunchnearby.R
import org.antonkozlenko.lunchnearby.Injection
import org.antonkozlenko.lunchnearby.api.PlacesSortCriteria
import org.antonkozlenko.lunchnearby.data.NetworkState
import org.antonkozlenko.lunchnearby.model.Restaurant
import java.lang.IllegalStateException


class SearchRestaurantsFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private val TAG = SearchRestaurantsFragment::class.java.simpleName

    private lateinit var viewModel: RestaurantsViewModel

    private lateinit var itemSelectionListener: OnRestaurantSelectionListener

    private lateinit var restaurants_list: RecyclerView
    private lateinit var restaurants_refresh: SwipeRefreshLayout
    private lateinit var search_restaurant: EditText
    private lateinit var sortBySpinner: Spinner

    private lateinit var adapter: RestaurantsAdapter

    private var sortByInitialized = false

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        // get the view model
        viewModel = ViewModelProviders.of(this, Injection.provideAppViewModelFactory(activity!!))
                .get(RestaurantsViewModel::class.java)

        if (context is OnRestaurantSelectionListener) {
            itemSelectionListener = context
        } else {
            throw IllegalStateException("Activity must implement selection listener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.restaurants.observe(this, Observer<PagedList<Restaurant>> {
            Log.d(TAG, "list: ${it?.size}")
            adapter.submitList(it)
        })
        viewModel.networkState.observe(this, Observer<NetworkState> {
            Log.d(TAG, "Status: ${it?.status}")
            it?.let {
                if (it == NetworkState.LOADING) {
                    Toast.makeText(activity, R.string.loading, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_search_restaurants, container, false)

        restaurants_list = layout.findViewById(R.id.restaurants_list)
        restaurants_refresh = layout.findViewById(R.id.restaurants_refresh)
        search_restaurant = layout.findViewById(R.id.search_restaurant)
        sortBySpinner = layout.findViewById(R.id.sort_by_spinner)

        // add dividers between RecyclerView's row items
        val decoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        restaurants_list.addItemDecoration(decoration)

        initAdapter()
        initSwipeToRefresh()
        initSortBySpinner()
        val query = savedInstanceState?.getString(LAST_SEARCH_QUERY) ?: DEFAULT_QUERY
        initSearch(query)
        viewModel.searchRestaurants(query)
        return layout
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(LAST_SEARCH_QUERY, viewModel.lastQueryValue())
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (sortByInitialized) {

            val selectedSort = parent!!.getItemAtPosition(position).toString()

            val sortCriteria = when (selectedSort) {
                getString(R.string.sort_by_distance) -> PlacesSortCriteria.DISTANCE
                else -> PlacesSortCriteria.BEST_MATCH
            }

            viewModel.setSortingCriteria(sortCriteria)
        } else {
            sortByInitialized = true
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun initSortBySpinner() {
        sortByInitialized = false
        val spinnerAdapter = ArrayAdapter.createFromResource(activity, R.array.sorting_options,
                android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        sortBySpinner.adapter = spinnerAdapter

        sortBySpinner.onItemSelectedListener = this
    }

    private fun initAdapter() {
        adapter = RestaurantsAdapter(itemSelectionListener)
        restaurants_list.adapter = adapter
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

        search_restaurant.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                updateRestaurantsListFromInput()
                true
            } else {
                false
            }
        }
        search_restaurant.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updateRestaurantsListFromInput()
                true
            } else {
                false
            }
        }
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

        fun newInstance(): SearchRestaurantsFragment {
            val fragment = SearchRestaurantsFragment()
            return fragment
        }
    }
}
