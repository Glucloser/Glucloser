package com.nlefler.glucloser.a.dataSource

import android.os.Parcelable
import com.nlefler.glucloser.a.models.BolusEvent
import com.nlefler.glucloser.a.models.Meal
import com.nlefler.glucloser.a.models.Snack
import javax.inject.Inject

/**
 * Created by Nathan Lefler on 7/2/15.
 */
class BolusEventFactory @Inject constructor(val mealFactory: MealFactory, val snackFactory: SnackFactory) {

    fun parcelableFromBolusEvent(bolusEvent: BolusEvent): Parcelable? {
        when (bolusEvent) {
            is Snack -> return snackFactory.parcelableFromSnack(bolusEvent)
            is Meal -> return mealFactory.parcelableFromMeal(bolusEvent)
            else -> return null
        }
    }
}
