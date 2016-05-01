package com.nlefler.glucloser.a.models

import java.util.Date

/**
 * Created by Nathan Lefler on 5/8/15.
 */
interface BolusEvent {
    val primaryId: String
    val date: Date
    val bolusPattern: BolusPattern?
    val carbs: Int
    val insulin: Float
    val beforeSugar: BloodSugar?
    val isCorrection: Boolean
    val foods: List<Food>
}
