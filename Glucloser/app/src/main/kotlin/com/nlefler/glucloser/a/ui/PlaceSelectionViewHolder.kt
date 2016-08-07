/**
 * Created by nathan on 11/9/15.
 */
package com.nlefler.glucloser.a.ui

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.dataSource.PlaceFactory
import com.nlefler.glucloser.a.models.Place
import com.nlefler.glucloser.a.models.parcelable.PlaceParcelable
import com.nlefler.glucloser.a.models.PlaceSelectionDelegate
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue
import javax.inject.Inject

/**
 * Created by nathan on 10/27/15.
 */
class PlaceSelectionViewHolder @Inject constructor(itemView: View, val delegate: PlaceSelectionDelegate?,
                                                   val placeFactory: PlaceFactory) : RecyclerView.ViewHolder(itemView) {

    var venue: NLFoursquareVenue? = null
    var place: Place? = null
    var detail1: TextView
    var detail2: TextView
    var clickListener: View.OnClickListener

    init {
        this.detail1 = itemView.findViewById(R.id.place_selection_list_item_detail1) as TextView
        this.detail2 = itemView.findViewById(R.id.place_selection_list_item_detail2) as TextView
        this.clickListener = View.OnClickListener {
            val placeParcelable = getPlaceParcelable()
            if (placeParcelable != null) {
                delegate?.placeSelected(placeParcelable)
            }
        }
        itemView.setOnClickListener(this.clickListener)
    }

    private fun getPlaceParcelable(): PlaceParcelable? {
        val nnPlace = place
        if (nnPlace != null) {
            return placeFactory.parcelableFromPlace(nnPlace)
        }
        else {
            return placeFactory.parcelableFromFoursquareVenue(venue)
        }
    }
}
