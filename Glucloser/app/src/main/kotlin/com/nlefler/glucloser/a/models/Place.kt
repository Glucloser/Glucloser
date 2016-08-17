package com.nlefler.glucloser.a.models

import io.requery.*
import java.util.*

/**
 * Created by Nathan Lefler on 12/11/14.
 */
@Entity
interface Place: Persistable {
    @get:Key
    var primaryId: String
    var name: String
    var foursquareId: String
    var latitude: Float
    var longitude: Float
    var visitCount: Int
    var needsUpload: Boolean

    // Resolving requery
    @get:OneToMany
    var meal: Meal?
}
