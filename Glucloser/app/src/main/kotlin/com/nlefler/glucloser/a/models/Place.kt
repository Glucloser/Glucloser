package com.nlefler.glucloser.a.models

import io.requery.Entity
import io.requery.Key
import java.util.*

/**
 * Created by Nathan Lefler on 12/11/14.
 */
@Entity
open class Place(
        @Key
        open val primaryId: String = UUID.randomUUID().toString(),
        open val name: String = "",
        open val foursquareId: String = UUID.randomUUID().toString(),
        open val latitude: Float = 0f,
        open val longitude: Float = 0f,
        open val visitCount: Int = 0
    ): Syncable {
}
