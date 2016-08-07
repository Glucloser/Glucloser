package com.nlefler.glucloser.a.dataSource

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.nlefler.glucloser.a.GlucloserApplication
import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.models.Place
import com.nlefler.glucloser.a.models.PlaceSelectionDelegate
import com.nlefler.glucloser.a.ui.PlaceSelectionViewHolder
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue
import javax.inject.Inject

/**
 * Created by Nathan Lefler on 12/20/14.
 */
public class PlaceSelectionRecyclerAdapter(private val delegate: PlaceSelectionDelegate,
                                           var mostUsedPlaces: List<Place>,
                                           var nearestVenues: List<NLFoursquareVenue>) : RecyclerView.Adapter<PlaceSelectionViewHolder>() {

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): PlaceSelectionViewHolder {
        val view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.place_selection_list_item, viewGroup, false)

        val dataFactory = GlucloserApplication.sharedApplication?.rootComponent
        val viewHolder = PlaceSelectionViewHolder(view, delegate, dataFactory!!.placeFactory())

        return viewHolder
    }

    // Replaces the contents of a view (invoked by the view holder)
    override fun onBindViewHolder(viewHolder: PlaceSelectionViewHolder, i: Int) {
        if (i < mostUsedPlaces.size) {
            val place = mostUsedPlaces[i]
            viewHolder.place = place
            viewHolder.detail1.text = place.name
        }
        else if (i - mostUsedPlaces.size < nearestVenues.size) {
            val idx = i - mostUsedPlaces.size
            val venue = nearestVenues[idx]
            viewHolder.venue = venue
            viewHolder.detail1.text = venue.name
        }
    }

    override fun getItemCount(): Int {
        return mostUsedPlaces.size + nearestVenues.size
    }
}
