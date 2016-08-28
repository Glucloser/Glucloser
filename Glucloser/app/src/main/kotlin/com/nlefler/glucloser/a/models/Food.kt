package com.nlefler.glucloser.a.models

import io.requery.*
import java.util.*

/**
 * Created by Nathan Lefler on 5/16/15.
 */
@Entity
interface Food: Persistable {
    @get:Key
    var primaryId: String
    var carbs: Int
    var foodName: String
    var insulin: Float

    // Trying to resolve requery annotation parsing issues
    @get:ManyToOne
    var meal: Meal?
}
