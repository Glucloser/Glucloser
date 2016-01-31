/**
 * Created by nathan on 11/9/15.
 */
package com.nlefler.glucloser.ui

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.nlefler.glucloser.R
import com.nlefler.glucloser.dataSource.PlaceFactory
import com.nlefler.glucloser.models.parcelable.PlaceParcelable
import com.nlefler.glucloser.models.PlaceSelectionDelegate
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue
import javax.inject.Inject

/**
 * Created by nathan on 10/27/15.
 */
public class PlaceSelectionViewHolder(itemView: View, delegate: PlaceSelectionDelegate?) : RecyclerView.ViewHolder(itemView) {
    lateinit var placeFactory: PlaceFactory
        @Inject set

    internal var venue: NLFoursquareVenue
    internal var placeName: TextView
    internal var placeDistance: TextView
    protected var clickListener: View.OnClickListener

    init {

        this.venue = NLFoursquareVenue()
        this.placeName = itemView.findViewById(R.id.place_selection_place_detail_name) as TextView
        this.placeDistance = itemView.findViewById(R.id.place_selection_place_detail_distance) as TextView
        this.clickListener = View.OnClickListener {
            var placeParcelable = getPlaceParcelable()
            if (placeParcelable != null) {
                delegate?.placeSelected(placeParcelable)
            }
        }
        itemView.setOnClickListener(this.clickListener)
    }

    private fun getPlaceParcelable(): PlaceParcelable? {
        return placeFactory.parcelableFromFoursquareVenue(this.venue)
    }
}
