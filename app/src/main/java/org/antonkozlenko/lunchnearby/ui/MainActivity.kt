package org.antonkozlenko.lunchnearby.ui

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import org.antonkozlenko.lunchnearby.R
import org.antonkozlenko.lunchnearby.Injection
import org.antonkozlenko.lunchnearby.model.Restaurant


class MainActivity : AppCompatActivity(), SearchRestaurantsFragment.OnRestaurantSelectionListener {

    private val TAG = MainActivity::class.java.simpleName
    private val LOCATION_REQUEST_CODE = 1001

    private val TAG_SEARCH_RESTAURANTS = SearchRestaurantsFragment::class.java.simpleName
    private val TAG_PLACE_DETAILS = PlaceDetailsFragment::class.java.simpleName

    private lateinit var viewModel: RestaurantsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get the view model
        viewModel = ViewModelProviders.of(this, Injection.provideAppViewModelFactory(this))
                .get(RestaurantsViewModel::class.java)

        setupPermissions()

        viewModel.locationPermissionGranted.observe(this, Observer<Boolean> {
            it?.let {
                if (it) {
                    searchRestaurants()
                } else {
                    setupPermissions()
                }
            }
        })

    }

    private fun setupPermissions() {
        Log.d(TAG, "Setup permissions")
        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission for location not granted")
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Permission to access location is required for this app to search places.")
                        .setTitle("Permission required")

                builder.setPositiveButton("OK") { dialog, id ->
                    Log.d(TAG, "Clicked")
                    requestPermissions()
                }

                val dialog = builder.create()
                dialog.show()
            } else {
                requestPermissions()
            }
        } else {
            viewModel.setLocationPermissionGranted(true)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    viewModel.setLocationPermissionGranted(false)
                    Log.d(TAG, "Permission has been denied by user")
                } else {
                    Log.d(TAG, "Permission has been granted by user")
                    viewModel.setLocationPermissionGranted(true)
                }
            }
        }
    }

    private fun requestPermissions() {
        Log.d(TAG, "Request permissions")
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_REQUEST_CODE)
    }


    override fun onRestaurantSelected(restaurant: Restaurant) {
        showRestaurantDetails(restaurant.id)
    }

    private fun searchRestaurants() {
        var searchFragment = supportFragmentManager.findFragmentByTag(TAG_SEARCH_RESTAURANTS)
        if (searchFragment == null) {
            searchFragment = SearchRestaurantsFragment.newInstance()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.root_layout, searchFragment, TAG_SEARCH_RESTAURANTS)
                    .commit()
        }
    }

    private fun showRestaurantDetails(placeId: String) {
        val detailsFragment =
                PlaceDetailsFragment.newInstance(placeId)
        supportFragmentManager.beginTransaction()
                .replace(R.id.root_layout, detailsFragment, TAG_PLACE_DETAILS)
                .addToBackStack(null)
                .commit()
    }
}
