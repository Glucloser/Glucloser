package com.nlefler.glucloser.dataSource

import android.os.Parcelable
import bolts.Continuation
import bolts.Task
import com.nlefler.glucloser.models.BolusPattern
import com.nlefler.glucloser.models.BolusPatternParcelable
import com.nlefler.glucloser.models.BolusRate
import com.parse.ParseObject
import com.parse.ParseQuery
import io.realm.RealmList
import java.util.*

/**
 * Created by nathan on 9/19/15.
 */
public class BolusPatternFactory {
    companion object {
        public fun BolusPatternFromParseObject(parseObj: ParseObject): BolusPattern {
            val pattern = BolusPattern()
            pattern.rateCount = parseObj.getInt(BolusPattern.RateCountFieldName)

            val rateParseObjs: List<ParseObject> = parseObj.getList(BolusPattern.RatesFieldName)
            for (rateParseObj in rateParseObjs) {
               pattern.rates.add(BolusRateFactory.BolusRateFromParseObject(rateParseObj))
            }

            return pattern
        }

        public fun ParcelableFromBolusPattern(pattern: BolusPattern): BolusPatternParcelable {
            val parcel = BolusPatternParcelable()
            parcel.rateCount = pattern.rateCount
            for (rate in pattern.rates) {
                parcel.rates.add(BolusRateFactory.ParcelableFromBolusRate(rate))
            }
            return parcel
        }

        public fun ParseObjectFromBolusPattern(pattern: BolusPattern): ParseObject {
            val prs = ParseObject.create(BolusPattern.ParseClassName)
            prs.put(BolusPattern.RateCountFieldName, pattern.rateCount)
            for (rate in pattern.rates) {
                prs.add(BolusPattern.RatesFieldName, BolusRateFactory.ParseObjectFromBolusRate(rate))
            }

            return prs
        }

        public fun CurrentBolusPattern(): Task<BolusPattern?> {
            val query = ParseQuery<ParseObject>(BolusPattern.ParseClassName)
            query.orderByDescending("updatedAt")
            query.setLimit(1)
            query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK)

            return query.getFirstInBackground().continueWithTask({ task ->
                // Get all rates
                task.getResult().fetchIfNeededInBackground<ParseObject>()
            }).continueWith({ task ->
                BolusPatternFromParseObject(task.getResult())
            })
        }

        public fun BolusPatternFromParcelable(parcelable: BolusPatternParcelable): BolusPattern {
            val pattern = BolusPattern()
            pattern.rateCount = parcelable.rateCount

            for (rateParcelable in parcelable.rates) {
                pattern.rates.add(BolusRateFactory.BolusRateFromParcelable(rateParcelable))
            }

            return pattern
        }
    }
}
