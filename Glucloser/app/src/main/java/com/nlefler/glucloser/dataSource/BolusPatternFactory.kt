package com.nlefler.glucloser.dataSource

import android.os.Parcelable
import bolts.Continuation
import bolts.Task
import com.nlefler.glucloser.models.BolusPattern
import com.nlefler.glucloser.models.BolusPatternParcelable
import com.nlefler.glucloser.models.BolusRate
import com.parse.ParseObject
import com.parse.ParseQuery
import io.realm.Realm
import io.realm.RealmList
import java.util.*

/**
 * Created by nathan on 9/19/15.
 */
public class BolusPatternFactory {
    companion object {
        public fun EmptyPattern(): BolusPattern {
            val rate = BolusRateFactory.EmptyRate()
            Realm.getDefaultInstance().beginTransaction()
            val pattern = BolusPatternForId("__glucloser_special_empty_bolus_pattern", Realm.getDefaultInstance(), true)
            pattern?.rateCount = 1
            pattern?.rates?.add(rate)
            Realm.getDefaultInstance().commitTransaction()

            return pattern!!
        }

        public fun BolusPatternFromParseObject(parseObj: ParseObject): BolusPattern? {
            val patternId = parseObj.getString(BolusPattern.IdFieldName) ?: return null
            var pattern: BolusPattern?

            val rates = ArrayList<BolusRate?>()
            val rateParseObjs: List<ParseObject> = parseObj.getList(BolusPattern.RatesFieldName)
            for (rateParseObj in rateParseObjs) {
                val rate = BolusRateFactory.BolusRateFromParseObject(rateParseObj)
                if (rate != null) {
                    rates.add(rate)
                }
            }

            Realm.getDefaultInstance().beginTransaction()
            pattern = BolusPatternForId(patternId, Realm.getDefaultInstance(), true)
            pattern?.rateCount = parseObj.getInt(BolusPattern.RateCountFieldName)

            pattern?.rates?.addAll(rates)
            Realm.getDefaultInstance().commitTransaction()

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

        public fun UpdateCurrentBolusPatternCache(): Task<BolusPattern?> {
            return FetchCurrentBolusPatternFromNetwork(true)
        }

        public fun FetchCurrentBolusPattern(): Task<BolusPattern?> {
            return FetchCurrentBolusPatternFromNetwork(false)
        }

        private fun FetchCurrentBolusPatternFromNetwork(fromNetwork: Boolean): Task<BolusPattern?> {
            val query = ParseQuery<ParseObject>(BolusPattern.ParseClassName)
            query.orderByDescending("updatedAt")
            query.setLimit(1)
            if (fromNetwork) {
                query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY)
            }
            else {
                query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK)
            }

            return query.getFirstInBackground().continueWithTask({ task ->
                // Get all rates
                task.getResult().fetchIfNeededInBackground<ParseObject>()
            }).continueWithTask({ task ->
                val parseObj = task.getResult()

                val rateParseObjs: List<ParseObject> = parseObj.getList(BolusPattern.RatesFieldName)
                val rateParseObjPromises = ArrayList<Task<ParseObject>>();
                for (rateParseObj in rateParseObjs) {
                    rateParseObjPromises.add(rateParseObj.fetchIfNeededInBackground())
                }
                Task.whenAll(rateParseObjPromises).continueWith({ task ->
                    BolusPatternFromParseObject(parseObj)
                })
            })
        }

        public fun BolusPatternFromParcelable(parcelable: BolusPatternParcelable): BolusPattern {
            val id = parcelable.id ?: UUID.randomUUID().toString()
            val rates = ArrayList<BolusRate>()

            for (rateParcelable in parcelable.rates) {
                rates.add(BolusRateFactory.BolusRateFromParcelable(rateParcelable))
            }

            Realm.getDefaultInstance().beginTransaction()
            val pattern = BolusPatternForId(id, Realm.getDefaultInstance(), true)
            pattern!!.rateCount = parcelable.rateCount
            pattern.rates.addAll(rates)

            Realm.getDefaultInstance().commitTransaction()

            return pattern
        }

        private fun BolusPatternForId(id: String, realm: Realm, create: Boolean): BolusPattern? {
            if (create && id.length() == 0) {
                val rate = realm.createObject<BolusPattern>(BolusPattern::class.java)
                return rate
            }

            val query = realm.where<BolusPattern>(BolusPattern::class.java)

            query.equalTo(BolusPattern.IdFieldName, id)
            var result: BolusPattern? = query.findFirst()

            if (result == null && create) {
                result = realm.createObject<BolusPattern>(BolusPattern::class.java)
                result!!.NLID = id
            }

            return result
        }
    }
}
