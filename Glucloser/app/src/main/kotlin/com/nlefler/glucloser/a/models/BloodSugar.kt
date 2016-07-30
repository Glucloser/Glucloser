package com.nlefler.glucloser.a.models

import io.requery.Entity
import io.requery.Generated
import io.requery.Key
import io.requery.Persistable
import java.util.*

/**
 * Created by Nathan Lefler on 1/4/15.
 */
@Entity
interface BloodSugar: Persistable {
    @get:Key
    @get:Generated
    @get:io.requery.ForeignKey
    var primaryId: String
    var readingValue: Int
    var recordedDate: Date
}
