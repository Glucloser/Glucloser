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
import javax.inject.Inject

/**
 * Created by nathan on 9/19/15.
 */
public class BolusPatternFactory {
    @Inject var realm: Realm? = null
    @Inject var bolusRateFactory: BolusRateFactory? = null

    public fun emptyPattern(): BolusPattern {
        val rate = bolusRateFactory?.emptyRate()
        realm?.beginTransaction()
        val pattern = bolusPatternForId("__glucloser_special_empty_bolus_pattern", true)
        pattern?.rateCount = 1
        pattern?.rates?.add(rate)
        realm?.commitTransaction()

        return pattern!!
    }

    public fun bolusPatternFromParseObject(parseObj: ParseObject): BolusPattern? {
        val patternId = parseObj.getString(BolusPattern.IdFieldName) ?: return null
        var pattern: BolusPattern?

        val rates = ArrayList<BolusRate?>()
        val rateParseObjs: List<ParseObject> = parseObj.getList(BolusPattern.RatesFieldName)
        for (rateParseObj in rateParseObjs) {
            val rate = bolusRateFactory?.bolusRateFromParseObject(rateParseObj)
            if (rate != null) {
                rates.add(rate)
            }
        }

        realm?.beginTransaction()
        pattern = bolusPatternForId(patternId, true)
        pattern?.rateCount = parseObj.getInt(BolusPattern.RateCountFieldName)

        pattern?.rates?.addAll(rates)
        realm?.commitTransaction()

        return pattern
    }

    public fun parcelableFromBolusPattern(pattern: BolusPattern): BolusPatternParcelable {
        val parcel = BolusPatternParcelable()
        parcel.rateCount = pattern.rateCount
        for (rate in pattern.rates) {
            parcel.rates.add(bolusRateFactory!!.parcelableFromBolusRate(rate))
        }
        return parcel
    }

    public fun parseObjectFromBolusPattern(pattern: BolusPattern): ParseObject {
        val prs = ParseObject.create(BolusPattern.ParseClassName)
        prs.put(BolusPattern.RateCountFieldName, pattern.rateCount)
        for (rate in pattern.rates) {
            prs.add(BolusPattern.RatesFieldName, bolusRateFactory?.parseObjectFromBolusRate(rate))
        }

        return prs
    }

    public fun updateCurrentBolusPatternCache(): Task<BolusPattern?> {
        return fetchCurrentBolusPatternFromNetwork(true)
    }

    public fun fetchCurrentBolusPattern(): Task<BolusPattern?> {
        return fetchCurrentBolusPatternFromNetwork(false)
    }

    private fun fetchCurrentBolusPatternFromNetwork(fromNetwork: Boolean): Task<BolusPattern?> {
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
                bolusPatternFromParseObject(parseObj)
            })
        })
    }

    public fun bolusPatternFromParcelable(parcelable: BolusPatternParcelable): BolusPattern {
        val id = parcelable.id ?: UUID.randomUUID().toString()
        val rates = ArrayList<BolusRate>()

        for (rateParcelable in parcelable.rates) {
            rates.add(bolusRateFactory!!.bolusRateFromParcelable(rateParcelable))
        }

        realm?.beginTransaction()
        val pattern = bolusPatternForId(id, true)
        pattern!!.rateCount = parcelable.rateCount
        pattern.rates.addAll(rates)

        realm?.commitTransaction()

        return pattern
    }

    private fun bolusPatternForId(id: String, create: Boolean): BolusPattern? {
        if (create && id.length() == 0) {
            val rate = realm?.createObject<BolusPattern>(BolusPattern::class.java)
            return rate
        }

        val query = realm?.where<BolusPattern>(BolusPattern::class.java)

        query?.equalTo(BolusPattern.IdFieldName, id)
        var result: BolusPattern? = query?.findFirst()

        if (result == null && create) {
            result = realm?.createObject<BolusPattern>(BolusPattern::class.java)
            result!!.NLID = id
        }

        return result
    }
}
