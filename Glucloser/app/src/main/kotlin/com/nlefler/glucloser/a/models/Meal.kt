package com.nlefler.glucloser.a.models

import io.requery.*
import java.util.*

/**
 * Created by Nathan Lefler on 12/11/14.
 */
@Entity
interface Meal: Persistable {
    @get:Key
    var primaryId: String
    var eatenDate: Date
    @get:ManyToOne
    var bolusPattern: BolusPattern?
    var carbs: Int
    var insulin: Float
    @get:OneToOne(mappedBy = "primaryId")
    @get:ForeignKey(referencedColumn = "primaryId")
    var beforeSugar: BloodSugar?
    var isCorrection: Boolean
    @get:OneToMany
    val foods: MutableSet<Food>
    @get:ManyToOne
    var place: Place?
    var needsUpload: Boolean
}
