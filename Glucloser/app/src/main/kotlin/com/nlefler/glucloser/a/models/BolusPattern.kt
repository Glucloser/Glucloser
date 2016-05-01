package com.nlefler.glucloser.a.models

import com.nlefler.glucloser.a.models.BolusRate
import java.util.*

/**
 * Created by nathan on 9/19/15.
 */
data class BolusPattern(
        val primaryId: String = UUID.randomUUID().toString(),
        val rates: List<BolusRate> = ArrayList<BolusRate>()
    ) {
}
