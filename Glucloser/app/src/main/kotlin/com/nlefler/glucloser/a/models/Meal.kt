package com.nlefler.glucloser.a.models

import java.util.*

/**
 * Created by Nathan Lefler on 12/11/14.
 */
data class Meal(
        override val primaryId: String = UUID.randomUUID().toString(),
        override val date: Date = Date(),
        override val bolusPattern: BolusPattern? = null,
        override val carbs: Int = 0,
        override val insulin: Float = 0f,
        override val beforeSugar: BloodSugar? = null,
        override val isCorrection: Boolean = false,
        override val foods: List<Food> = ArrayList(),
        override val place: Place? = null
    ) : BolusEvent, HasPlace, Syncable {
}
