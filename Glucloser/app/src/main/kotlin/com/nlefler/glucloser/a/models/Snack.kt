package com.nlefler.glucloser.a.models

import io.requery.Entity
import io.requery.Key
import io.requery.OneToMany
import java.util.*

/**
 * Created by Nathan Lefler on 5/8/15.
 */
@Entity
open class Snack(
        @Key
        override open val primaryId: String = UUID.randomUUID().toString(),
        override open val date: Date,
        override open val bolusPattern: BolusPattern?,
        override open val carbs: Int,
        override open val insulin: Float,
        override open val beforeSugar: BloodSugar?,
        override open val isCorrection: Boolean,
        @OneToMany
        override open val foods: List<Food>
    ) : BolusEvent, Syncable {
}
