package com.nlefler.glucloser.a.models

import io.requery.Entity
import io.requery.Generated
import io.requery.Key
import io.requery.Persistable
import java.util.*

/**
 * Created by Nathan Lefler on 12/11/14.
 */
@Entity
interface Meal: Persistable, HasPlace, BolusEvent {
    @get:Key
    @get:Generated
    @get:io.requery.ForeignKey
    var primaryId: String
    var eatenDate: Date
    var bolusPattern: BolusPattern?
    var carbs: Int
    var insulin: Float
    var beforeSugar: BloodSugar?
    var isCorrection: Boolean
    var foods: MutableList<Food>
    var place: Place?
}
