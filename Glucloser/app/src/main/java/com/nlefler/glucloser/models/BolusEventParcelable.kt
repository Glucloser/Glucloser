package com.nlefler.glucloser.models

import java.util.Date

/**
 * Created by Nathan Lefler on 5/8/15.
 */
public interface BolusEventParcelable {
    var date: Date
    var bolusPatternParcelable: BolusPatternParcelable
    var carbs: Int
    var insulin: Float
    var bloodSugarParcelable: BloodSugarParcelable?
    var isCorrection: Boolean
    var foodParcelable: List<FoodParcelable>
}
