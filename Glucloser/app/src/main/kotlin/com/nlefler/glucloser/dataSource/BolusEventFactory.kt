package com.nlefler.glucloser.dataSource

import android.os.Parcelable
import com.nlefler.glucloser.models.BolusEvent
import com.nlefler.glucloser.models.Meal
import com.nlefler.glucloser.models.Snack
import javax.inject.Inject

/**
 * Created by Nathan Lefler on 7/2/15.
 */
public class BolusEventFactory @Inject constructor(val mealFactory: MealFactory, val snackFactory: SnackFactory) {

    public fun parcelableFromBolusEvent(bolusEvent: BolusEvent): Parcelable? {
        when (bolusEvent) {
            is Snack -> return snackFactory.parcelableFromSnack(bolusEvent)
            is Meal -> return mealFactory.parcelableFromMeal(bolusEvent)
            else -> return null
        }
    }
}
