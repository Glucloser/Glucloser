package com.nlefler.glucloser.a.models

import io.requery.Entity
import io.requery.Key
import java.util.*

/**
 * Created by nathan on 3/26/16.
 */
@Entity
open class SensorReading(
        @Key
        open val primaryId: String = UUID.randomUUID().toString(),
        open val timestamp: Date,
        open val reading: Int) {
}
