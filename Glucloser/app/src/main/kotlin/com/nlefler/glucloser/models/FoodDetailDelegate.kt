package com.nlefler.glucloser.models

import com.nlefler.glucloser.models.parcelable.FoodParcelable

/**
 * Created by Nathan Lefler on 5/17/15.
 */
public interface FoodDetailDelegate {
    public fun foodDetailUpdated(foodParcelable: FoodParcelable)
}
