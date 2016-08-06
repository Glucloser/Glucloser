package com.nlefler.glucloser.a.models

import io.requery.*
import java.util.*

/**
 * Created by Nathan Lefler on 5/8/15.
 */
@Entity
interface Snack: BolusEvent, Persistable {
    @get:Key
    override var primaryId: String
    override var eatenDate: Date
    @get:ManyToOne
    override var bolusPattern: BolusPattern?
    override var carbs: Int
    override var insulin: Float
    @get:OneToOne(mappedBy = "primaryId")
    @get:ForeignKey(referencedColumn = "primaryId")
    override var beforeSugar: BloodSugar?
    override var isCorrection: Boolean
    @get:OneToMany
    override var foods: MutableList<Food>
}
