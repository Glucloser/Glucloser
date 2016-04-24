package com.nlefler.glucloser.a.models

import com.nlefler.glucloser.a.models.parcelable.PlaceParcelable

/**
 * Created by Nathan Lefler on 12/24/14.
 */
interface PlaceSelectionDelegate {
    fun placeSelected(placeParcelable: PlaceParcelable)
}
