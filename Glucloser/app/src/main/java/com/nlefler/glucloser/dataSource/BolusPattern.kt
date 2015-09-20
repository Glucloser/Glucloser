package com.nlefler.glucloser.dataSource

import com.nlefler.glucloser.models.BolusRate

/**
 * Created by nathan on 9/19/15.
 */
public class BolusPattern {
    var rateCount: Int? = null
    var rates: List<BolusRate>? = null

    companion object {
        val ParseClassName = "BolusPattern"
        val ParseKeyRateCount = "rateCount"
        val ParseKeyRates = "rates"
    }
}
