package org.antonkozlenko.lunchnearby.ui

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.antonkozlenko.lunchnearby.R
import org.antonkozlenko.lunchnearby.Injection
import org.antonkozlenko.lunchnearby.model.Restaurant


class MainActivity : AppCompatActivity(), SearchRestaurantsFragment.OnRestaurantSelectionListener {

    private lateinit var viewModel: SearchRestaurantsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get the view model
        viewModel = ViewModelProviders.of(this, Injection.provideAppViewModelFactory(this))
                .get(SearchRestaurantsViewModel::class.java)

    }

    override fun onRestaurantSelected(restaurant: Restaurant) {
        showRestaurantDetails(restaurant.id)
    }

    private fun showRestaurantDetails(placeId: String) {
        val detailsFragment =
                PlaceDetailsFragment.newInstance(placeId)
        supportFragmentManager.beginTransaction()
                .replace(R.id.root_layout, detailsFragment, "restaurantDetails")
                .addToBackStack(null)
                .commit()
    }
}
