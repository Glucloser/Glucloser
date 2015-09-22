package com.nlefler.glucloser.dataSource

import com.nlefler.glucloser.models.BolusRate
import com.parse.ParseObject
import com.parse.ParseQuery

import bolts.Task
import com.nlefler.glucloser.models.BolusRateParcelable

/**
 * Created by nathan on 9/1/15.
 */
public class BolusRateFactory {
    companion object {
        public fun BolusRateFromParcelable(parcelable: BolusRateParcelable): BolusRate {
            val rate = BolusRate()
            rate.ordinal = parcelable.ordinal
            rate.rate = parcelable.rate
            rate.startTime = parcelable.startTime

            return rate
        }

        public fun BolusRateFromParseObject(parseObj: ParseObject): BolusRate? {
            if (!parseObj.getClassName().equals(BolusRate.ParseClassName)) {
                return null;
            }
            val rate = BolusRate()
            rate.ordinal = parseObj.getInt(BolusRate.OridnalFieldName)
            rate.rate = parseObj.getInt(BolusRate.RateFieldName)
            rate.startTime = parseObj.getInt(BolusRate.StartTimeFieldName)
            return rate
        }
    }
}
