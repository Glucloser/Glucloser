package com.nlefler.glucloser.a.models

import com.nlefler.glucloser.a.models.BolusRate
import io.requery.*
import java.util.*

/**
 * Created by nathan on 9/19/15.
 */
@Entity
interface BolusPattern: Persistable {
    @get:Key
    @get:ForeignKey
    var primaryId: String
    @get:OneToMany
    var rates: MutableList<BolusRate>

    // Resolving requery processing
    @get:OneToMany
    var snack: Snack?
    @get:OneToMany
    var meal: Meal?
}
