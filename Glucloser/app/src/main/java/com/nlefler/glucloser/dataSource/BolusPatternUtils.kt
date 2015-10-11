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
            val curMilSecs = cal.get(Calendar.HOUR_OF_DAY) * 60 * 60 +
                            cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND) * 1000

            var activeRate: BolusRate? = null
            for (rate in bolusPattern.rates) {
                if (rate.startTime?.compareTo(curMilSecs) == -1) {
                    activeRate = rate
                    break;
                }
            }
            if (activeRate == null) {
                activeRate = bolusPattern.rates.last()
            }

            if (activeRate?.rate == null) {
                return 0f
            }
            return carbValue.toFloat() / activeRate?.rate!!.toFloat()
        }
    }
}
