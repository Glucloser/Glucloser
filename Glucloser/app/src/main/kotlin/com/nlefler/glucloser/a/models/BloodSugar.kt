package com.nlefler.glucloser.a.models

import io.requery.*
import java.util.*

/**
 * Created by Nathan Lefler on 1/4/15.
 */
@Entity
interface BloodSugar: SugarReading, Persistable {
    @get:Key
    var primaryId: String
    override var readingValue: Int
    override var recordedDate: Date
}
