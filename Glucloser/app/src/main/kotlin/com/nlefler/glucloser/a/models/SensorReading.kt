package com.nlefler.glucloser.a.models

import io.requery.Entity
import io.requery.Generated
import io.requery.Key
import io.requery.Persistable
import java.util.*

/**
 * Created by nathan on 3/26/16.
 */
@Entity
interface SensorReading: Persistable {
    @get:Key
    var primaryId: String
    var readingTimestamp: Date
    var reading: Int
}
