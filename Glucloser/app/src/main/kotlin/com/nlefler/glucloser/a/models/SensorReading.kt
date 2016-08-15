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
interface SensorReading: SugarReading, Persistable {
    @get:Key
    var primaryId: String
    override var recordedDate: Date
    override var readingValue: Int
}
