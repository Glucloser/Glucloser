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
interface Place: Persistable {
    @get:Key
    @get:Generated
    @get:io.requery.ForeignKey
    var primaryId: String
    var name: String
    var foursquareId: String
    var latitude: Float
    var longitude: Float
    var visitCount: Int
}
