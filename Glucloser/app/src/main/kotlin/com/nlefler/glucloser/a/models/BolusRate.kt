package com.nlefler.glucloser.a.models

import io.requery.Entity
import io.requery.Key
import java.util.*

/**
 * Created by nathan on 8/30/15.
 */
@Entity
open class BolusRate(
        @Key
        open val primaryId: String = UUID.randomUUID().toString(),
        open val ordinal: Int,
        open val carbsPerUnit: Int,
        open val startTime: Int
    ) {
}
