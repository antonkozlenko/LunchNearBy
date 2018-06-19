package org.antonkozlenko.lunchnearby.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import org.antonkozlenko.lunchnearby.GlideApp
import org.antonkozlenko.lunchnearby.Injection
import org.antonkozlenko.lunchnearby.R
import org.antonkozlenko.lunchnearby.data.NetworkState
import org.antonkozlenko.lunchnearby.model.RestaurantDetails

class PlaceDetailsFragment: Fragment() {
    private val TAG = PlaceDetailsFragment::class.java.simpleName

    private lateinit var viewModel: SearchRestaurantsViewModel

    private lateinit var name: TextView
    private lateinit var address: TextView
    private lateinit var icon: ImageView
    private lateinit var rating: TextView
    private lateinit var localPhone: TextView
    private lateinit var internationalPhone: TextView
    private lateinit var website: TextView

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        // get the view model
        viewModel = ViewModelProviders.of(this, Injection.provideAppViewModelFactory(activity!!))
                .get(SearchRestaurantsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_place_details, container, false)

        name = layout.findViewById(R.id.place_name)
        address = layout.findViewById(R.id.place_address)
        icon = layout.findViewById(R.id.place_icon)
        rating = layout.findViewById(R.id.place_rating)
        localPhone = layout.findViewById(R.id.place_local_phone)
        internationalPhone = layout.findViewById(R.id.place_international_phone)
        website = layout.findViewById(R.id.place_website)

        viewModel.restaurantDetails.observe(this, Observer<RestaurantDetails> {
            it?.let {
                showDetails(it)
            } ?: showFailure()
        })

        viewModel.networkState.observe(this, Observer<NetworkState> {
            Toast.makeText(activity!!, "Status ${it?.status}", Toast.LENGTH_SHORT).show()
        })

        return layout
    }

    override fun onResume() {
        super.onResume()
        arguments?.get(PLACE_ID)?.let {
            viewModel.getRestaurantDetails(it as String)
        } ?: showFailure()

        Log.d(TAG, "DATA: ${viewModel.restaurantDetails.value}")
    }

    private fun showDetails(placeDetails: RestaurantDetails) {
        name.text = placeDetails.name
        address.text = placeDetails.address
        rating.text = placeDetails.rating.toString()
        localPhone.text = placeDetails?.formattedPhoneNumber ?: "not available"
        internationalPhone.text = placeDetails?.internationalPhoneNumber ?: "not available"
        website.text = placeDetails.website ?: "not available"

        placeDetails.website?.let {url ->
            website.text = url
            website.isClickable = true
            website.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                website.context.startActivity(intent)
            }
        } ?: let {
            website.isClickable = false
            website.text = getString(R.string.no_results)
        }


        GlideApp.with(this)
                .load(placeDetails.icon)
                .into(icon)
    }

    private fun showFailure() {
        Toast.makeText(activity!!, "Failed to get details", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val PLACE_ID = "PLACE_ID"

        fun newInstance(placeId: String): PlaceDetailsFragment {
            val args = Bundle()
            args.putString(PLACE_ID, placeId)
            val fragment = PlaceDetailsFragment()
            fragment.arguments = args
            return fragment
        }
    }


}