package com.nlefler.glucloser.models.parcelable

import android.os.Parcelable
import com.nlefler.glucloser.models.parcelable.FoodParcelable
import java.util.Date

/**
 * Created by Nathan Lefler on 5/8/15.
 */
public interface BolusEventParcelable: Parcelable {
    var id: String
    var date: Date
    var bolusPatternParcelable: BolusPatternParcelable?
    var carbs: Int
    var insulin: Float
    var bloodSugarParcelable: BloodSugarParcelable?
    var isCorrection: Boolean
    var foodParcelables: List<FoodParcelable>
}
