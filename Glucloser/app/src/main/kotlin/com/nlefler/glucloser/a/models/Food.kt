package com.nlefler.glucloser.a.models

import io.requery.*
import java.util.*

/**
 * Created by Nathan Lefler on 5/16/15.
 */
@Entity
interface Food: Persistable {
    @get:Key
    @get:ForeignKey
    var primaryID: String
    var carbs: Int
    var foodName: String

    // Trying to resolve requery annotation parsing issues
    @get:ManyToOne
    var meal: Meal?
    @get:ManyToOne
    var snack: Snack?
}
