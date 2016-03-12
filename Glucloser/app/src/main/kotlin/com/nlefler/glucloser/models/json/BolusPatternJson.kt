package com.nlefler.glucloser.models.json

import com.nlefler.glucloser.models.BolusRate

/**
 * Created by nathan on 1/31/16.
 */
data class BolusPatternJson(
        val rates: List<BolusRateJson>
) {

}
