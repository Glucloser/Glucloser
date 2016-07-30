package com.nlefler.glucloser.a.models

import io.requery.Entity
import io.requery.Generated
import io.requery.Key
import io.requery.Persistable
import java.util.*

/**
 * Created by nathan on 8/30/15.
 */
@Entity
interface BolusRate: Persistable {
        @get:Key
        @get:Generated
        @get:io.requery.ForeignKey
        var primaryId: String
        var ordinal: Int
        var carbsPerUnit: Int
        var startTime: Int
}

