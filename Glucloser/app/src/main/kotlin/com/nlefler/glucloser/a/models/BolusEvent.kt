package com.nlefler.glucloser.a.models

import java.util.Date

/**
 * Created by Nathan Lefler on 5/8/15.
 */
interface BolusEvent {
    open val primaryId: String
    open val date: Date
    open val bolusPattern: BolusPattern?
    open val carbs: Int
    open val insulin: Float
    open val beforeSugar: BloodSugar?
    open val isCorrection: Boolean
    open val foods: List<Food>
}
