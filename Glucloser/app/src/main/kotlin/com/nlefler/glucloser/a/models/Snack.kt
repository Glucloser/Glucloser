package com.nlefler.glucloser.a.models

import io.requery.*
import java.util.*

/**
 * Created by Nathan Lefler on 5/8/15.
 */
@Entity
interface Snack: Persistable {
    var primaryId: String
    var eatenDate: Date
    var bolusPattern: BolusPattern?
    var carbs: Int
    var insulin: Float
    var beforeSugar: BloodSugar?
    var isCorrection: Boolean
    var foods: MutableList<Food>
}
