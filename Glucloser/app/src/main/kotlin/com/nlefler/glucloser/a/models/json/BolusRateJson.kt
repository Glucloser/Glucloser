package com.nlefler.glucloser.a.models.json

/**
 * Created by nathan on 1/31/16.
 */
data class BolusRateJson(
        val ordinal: Int,
        val carbsPerUnit: Int,
        val unit: String,
        val startTime: Int
    ) {
}
