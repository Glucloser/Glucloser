package com.nlefler.glucloser.models.json

/**
 * Created by nathan on 1/31/16.
 */
public data class BolusRateJson(
        val primaryId: String,
        val ordinal: Int,
        val carbsPerUnit: Int,
        val startTime: Int
    ) {
}
