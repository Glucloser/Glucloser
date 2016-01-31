package com.nlefler.glucloser.dataSource

import com.nlefler.glucloser.models.BolusPattern
import com.nlefler.glucloser.models.BolusRate
import java.util.*

/**
 * Created by nathan on 10/10/15.
 */
public class BolusPatternUtils {
    companion object {
        public fun InsulinForCarbsAtCurrentTime(bolusPattern: BolusPattern, carbValue: Int): Float {
            val cal = Calendar.getInstance()
            val curMilSecs = cal.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000 +
                            cal.get(Calendar.MINUTE) * 60 * 1000 + cal.get(Calendar.SECOND) * 1000

            var sortedRates = bolusPattern.rates.sortedBy { rate -> rate.ordinal }
            var activeRate: BolusRate? = null
            for (rate in sortedRates) {
                if (curMilSecs < rate.startTime) {
                    activeRate = rate
                    break;
                }
            }
            if (activeRate == null && sortedRates.last().startTime.compareTo(curMilSecs) != 1) {
                activeRate = sortedRates.last()
            }

            if (activeRate?.carbsPerUnit == null) {
                return 0f
            }
            return carbValue.toFloat() / activeRate?.carbsPerUnit!!.toFloat()
        }
    }
}
