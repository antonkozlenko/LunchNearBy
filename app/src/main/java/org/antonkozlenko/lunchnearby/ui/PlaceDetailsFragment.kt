package org.antonkozlenko.lunchnearby.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.antonkozlenko.lunchnearby.R

class PlaceDetailsFragment: Fragment() {
    private lateinit var name: TextView
    private lateinit var address: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_place_details, container, false)



        return layout
    }


}