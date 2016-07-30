package com.nlefler.glucloser.a.models.json

import com.nlefler.glucloser.a.models.BolusRate

/**
 * Created by nathan on 1/31/16.
 */
data class BolusPatternJson(
        val primaryId: String,
        val rates: List<BolusRateJson>
) {

}
