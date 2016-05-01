package com.nlefler.glucloser.a.models

import java.util.*

/**
 * Created by nathan on 8/30/15.
 */
data class BolusRate(
        val primaryId: String = UUID.randomUUID().toString(),
        val ordinal: Int = 0,
        val carbsPerUnit: Int = 0,
        val startTime: Int = 0
    ) {
}
