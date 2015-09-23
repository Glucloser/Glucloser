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

        public fun ParcelableFromBolusRate(rate: BolusRate): BolusRateParcelable {
            val parcel = BolusRateParcelable()
            parcel.ordinal = rate.ordinal
            parcel.rate = rate.rate
            parcel.startTime = rate.startTime

            return parcel
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

        public fun ParseObjectFromBolusRate(rate: BolusRate): ParseObject {
            val prs = ParseObject.create(BolusRate.ParseClassName)
            prs.put(BolusRate.OridnalFieldName, rate.ordinal)
            prs.put(BolusRate.RateFieldName, rate.rate)
            prs.put(BolusRate.StartTimeFieldName, rate.startTime)

            return prs
        }
    }
}
