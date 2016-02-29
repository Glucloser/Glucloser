package com.nlefler.glucloser.models.json

import com.nlefler.glucloser.models.BolusRate

/**
 * Created by nathan on 1/31/16.
 */
public data class BolusPatternJson(
        val primaryId: String,
        val rateCount: Int,
        val rates: List<BolusRateJson>
) {

}
