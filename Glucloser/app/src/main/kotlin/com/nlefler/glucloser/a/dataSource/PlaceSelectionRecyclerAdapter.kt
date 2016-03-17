package com.nlefler.glucloser.a.dataSource

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.nlefler.glucloser.a.GlucloserApplication
import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.models.PlaceSelectionDelegate
import com.nlefler.glucloser.a.ui.PlaceSelectionViewHolder
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue
import javax.inject.Inject

/**
 * Created by Nathan Lefler on 12/20/14.
 */
public class PlaceSelectionRecyclerAdapter(private val delegate: PlaceSelectionDelegate,
                                           private var venues: List<NLFoursquareVenue>?) : RecyclerView.Adapter<PlaceSelectionViewHolder>() {

    public fun setVenues(venues: List<NLFoursquareVenue>) {
        this.venues = venues
        notifyDataSetChanged()
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): PlaceSelectionViewHolder {
        val view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.place_selection_list_item, viewGroup, false)

        val dataFactory = GlucloserApplication.sharedApplication?.rootComponent
        val viewHolder = PlaceSelectionViewHolder(view, delegate, dataFactory!!.placeFactory())

        return viewHolder
    }

    // Replaces the contents of a view (invoked by the view holder)
    override fun onBindViewHolder(viewHolder: PlaceSelectionViewHolder, i: Int) {
        if (i >= this.venues!!.size) {
            return
        }

        val venue = this.venues!!.get(i)
        viewHolder.venue = venue
        viewHolder.placeName.setText(venue.name)
        // TODO: Localized format string
        viewHolder.placeDistance.setText("${venue.location.distance} meters")
    }

    override fun getItemCount(): Int {
        return this.venues!!.size
    }
}
