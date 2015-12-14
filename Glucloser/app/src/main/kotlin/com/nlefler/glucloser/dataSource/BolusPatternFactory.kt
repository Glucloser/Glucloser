package com.nlefler.glucloser.dataSource

import android.os.Parcelable
import bolts.Continuation
import bolts.Task
import bolts.TaskCompletionSource
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
public class BolusPatternFactory @Inject constructor(val realm: Realm, val bolusRateFactory: BolusRateFactory) {

    public fun emptyPattern(): Task<BolusPattern> {
        val task = TaskCompletionSource<BolusPattern>()

        bolusRateFactory.emptyRate().continueWith { rateTask ->
            if (rateTask.isFaulted) {
                task.trySetError(Exception("Unable to create BolusRate"))
            }
            else {
                realm.executeTransaction { realm ->
                    val pattern = bolusPatternForId("__glucloser_special_empty_bolus_pattern", true)
                    pattern?.rateCount = 1
                    pattern?.rates?.add(rateTask.result)

                    task.trySetResult(pattern)
                }
            }
        }

        return task.task
    }

    public fun bolusPatternFromParseObject(parseObj: ParseObject): Task<BolusPattern?> {
        val patternId = parseObj.getString(BolusPattern.IdFieldName) ?: return Task.forError(Exception("Invalid Parse Object"))

        val ratePromises = ArrayList<Task<BolusRate?>>()
        val rates = ArrayList<BolusRate>()

        val rateParseObjs: List<ParseObject> = parseObj.getList(BolusPattern.RatesFieldName)
        for (rateParseObj in rateParseObjs) {
            val ratePromise = TaskCompletionSource<BolusRate?>()
            ratePromises.add(ratePromise.task)
            bolusRateFactory.bolusRateFromParseObject(rateParseObj).continueWith({ task ->
                if (task.isFaulted) {
                    ratePromise.trySetError(Exception("Unable to create BolusRate"))
                } else {
                    val rate = task.result
                    rates.add(rate!!)
                    ratePromise.trySetResult(task.result)
                }
            })
        }

        val patternPromise = TaskCompletionSource<BolusPattern?>()
        Task.whenAll(ratePromises).continueWith {
            realm.executeTransaction { realm ->
                val pattern = bolusPatternForId(patternId, true)
                pattern?.rateCount = parseObj.getInt(BolusPattern.RateCountFieldName)
                pattern?.rates?.addAll(rates)

                patternPromise.trySetResult(pattern)
            }
        }

        return patternPromise.task
    }

    public fun parcelableFromBolusPattern(pattern: BolusPattern): BolusPatternParcelable {
        val parcel = BolusPatternParcelable()
        parcel.rateCount = pattern.rateCount
        for (rate in pattern.rates) {
            parcel.rates.add(bolusRateFactory.parcelableFromBolusRate(rate))
        }
        return parcel
    }

    public fun parseObjectFromBolusPattern(pattern: BolusPattern): ParseObject {
        val prs = ParseObject.create(BolusPattern.ParseClassName)
        prs.put(BolusPattern.RateCountFieldName, pattern.rateCount)
        for (rate in pattern.rates) {
            prs.add(BolusPattern.RatesFieldName, bolusRateFactory.parseObjectFromBolusRate(rate))
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

        val patternTask = TaskCompletionSource<BolusPattern?>()
        var patternParseObj: ParseObject? = null

        query.getFirstInBackground().continueWithTask({ task ->
            // Get all rates
            task.getResult().fetchIfNeededInBackground<ParseObject>()
        }).continueWithTask({ task ->
            val dlTask = TaskCompletionSource<ParseObject?>()

            patternParseObj = task.getResult()
            if (patternParseObj == null) {
                dlTask.trySetError(Exception("Unable to fetch current carb ratio pattern"))
            }
            else {

                val rateParseObjs: List<ParseObject> = patternParseObj!!.getList(BolusPattern.RatesFieldName)
                val rateParseObjPromises = ArrayList<Task<ParseObject>>()
                for (rateParseObj in rateParseObjs) {
                    rateParseObjPromises.add(rateParseObj.fetchIfNeededInBackground<ParseObject>())
                }
                Task.whenAll(rateParseObjPromises).continueWith { task ->
                    dlTask.trySetResult(patternParseObj)
                }
            }
            dlTask.task
        }).continueWith({ task ->
            val parseObj = task.result
            if (parseObj != null) {
                bolusPatternFromParseObject(parseObj).continueWith { task ->
                    if (task.isFaulted) {
                        patternTask.trySetError(task.error)
                    }
                    else {
                        patternTask.trySetResult(task.result)
                    }
                }
            }
            else {
                patternTask.trySetError(Exception("Unable to parse ParseObject into BolusPattern"))
            }
        })
        return patternTask.task
    }

    public fun bolusPatternFromParcelable(parcelable: BolusPatternParcelable): Task<BolusPattern?> {
        val patternTask = TaskCompletionSource<BolusPattern?>()

        val id = parcelable.id ?: UUID.randomUUID().toString()
        val rates = ArrayList<BolusRate>()
        val ratePromises = ArrayList<Task<BolusRate>>()

        for (rateParcelable in parcelable.rates) {
            ratePromises.add(bolusRateFactory.bolusRateFromParcelable(rateParcelable).continueWithTask { rateTask ->
                if (!rateTask.isFaulted) {
                    rates.add(rateTask.result)
                }
                rateTask
            })
        }

        Task.whenAll(ratePromises).continueWith { task ->
            val pattern = bolusPatternForId(id, true)
            pattern?.rateCount = parcelable.rateCount
            pattern?.rates?.addAll(rates)

            patternTask.trySetResult(pattern)
        }

        return patternTask.task
    }

    private fun bolusPatternForId(id: String, create: Boolean): BolusPattern? {
        if (create && id.length == 0) {
            val rate = realm.createObject<BolusPattern>(BolusPattern::class.java)
            return rate
        }

        val query = realm.where<BolusPattern>(BolusPattern::class.java)

        query?.equalTo(BolusPattern.IdFieldName, id)
        var result: BolusPattern? = query?.findFirst()

        if (result == null && create) {
            result = realm.createObject<BolusPattern>(BolusPattern::class.java)
            result!!.NLID = id
        }

        return result
    }
}
