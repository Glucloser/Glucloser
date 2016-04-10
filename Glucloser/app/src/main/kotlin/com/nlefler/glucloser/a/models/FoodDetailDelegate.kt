package com.nlefler.glucloser.a.models

import com.nlefler.glucloser.a.models.parcelable.FoodParcelable

/**
 * Created by Nathan Lefler on 5/17/15.
 */
public interface FoodDetailDelegate {
    public fun foodDetailUpdated(foodParcelable: FoodParcelable)
}
