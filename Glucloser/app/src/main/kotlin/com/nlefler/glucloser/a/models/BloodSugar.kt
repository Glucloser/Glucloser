package com.nlefler.glucloser.a.models

import io.requery.*
import java.util.*

/**
 * Created by Nathan Lefler on 1/4/15.
 */
@Entity
interface BloodSugar: Persistable {
    @get:Key
    @get:ForeignKey
    var primaryId: String
    var readingValue: Int
    var recordedDate: Date


    // Resolving requery processing
    @get:OneToOne(mappedBy = "primaryId")
    var snack: Snack?
    @get:OneToOne(mappedBy = "primaryId")
    var meal: Meal?
}
