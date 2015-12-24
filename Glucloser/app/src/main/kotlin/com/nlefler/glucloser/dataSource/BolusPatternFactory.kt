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
public class BolusPatternFactory @Inject constructor(val realmManager: RealmManager, val bolusRateFactory: BolusRateFactory) {

    public fun emptyPattern(): Task<BolusPattern?> {
        return bolusRateFactory.emptyRate().continueWithTask(Continuation<BolusRate?, Task<BolusPattern?>> { rateTask ->
            if (rateTask.isFaulted) {
                return@Continuation Task.forError(Exception("Unable to create BolusRate"))
            }
            else {
                return@Continuation bolusPatternForId("__glucloser_special_empty_bolus_pattern", true).continueWithTask(Continuation<BolusPattern?, Task<BolusPattern?>>{ task ->
                    val realmTask = TaskCompletionSource<BolusPattern?>()
                    return@Continuation realmManager.executeTransaction(Realm.Transaction { realm ->
                        val pattern = task.result
                        pattern?.rateCount = 1
                        pattern?.rates?.add(rateTask.result)
                        realmTask.trySetResult(pattern)
                    }, realmTask.task)
                })
            }
        })
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


        return Task.whenAll(ratePromises).continueWithTask(Continuation<Void, Task<BolusPattern?>> {
            val patternPromise = bolusPatternForId(patternId, true)
            patternPromise.continueWithTask(Continuation<BolusPattern?, Task<BolusPattern?>>{ task ->
                val realmTask = TaskCompletionSource<BolusPattern?>()
                return@Continuation realmManager.executeTransaction(Realm.Transaction{ realm ->
                    if (task.isFaulted) {
                        realmTask.trySetError(task.error)
                        return@Transaction
                    }
                    val pattern = task.result
                    pattern?.rateCount = parseObj.getInt(BolusPattern.RateCountFieldName)
                    pattern?.rates?.addAll(rates)
                    realmTask.trySetResult(pattern)
                }, realmTask.task)
            })
        })
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
        var patternParseObj: ParseObject?

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
        val ratePromises = ArrayList<Task<BolusRate?>>()

        for (rateParcelable in parcelable.rates) {
            ratePromises.add(bolusRateFactory.bolusRateFromParcelable(rateParcelable).continueWithTask { rateTask ->
                if (!rateTask.isFaulted && rateTask.result != null) {
                    rates.add(rateTask.result!!)
                }
                rateTask
            })
        }

        Task.whenAll(ratePromises).continueWithTask({ task ->
            bolusPatternForId(id, true)
        }).continueWith { task ->
            val pattern = task.result
            pattern?.rateCount = parcelable.rateCount
            pattern?.rates?.addAll(rates)

            patternTask.trySetResult(pattern)
        }

        return patternTask.task
    }

    private fun bolusPatternForId(id: String, create: Boolean): Task<BolusPattern?> {
        val patternTask = TaskCompletionSource<BolusPattern?>()
        realmManager.executeTransaction(Realm.Transaction { realm ->
            if (create && id.length == 0) {
                val pattern = realm.createObject<BolusPattern>(BolusPattern::class.java)
                patternTask.trySetResult(pattern)
                return@Transaction
            }

            val query = realm.where<BolusPattern>(BolusPattern::class.java)

            query?.equalTo(BolusPattern.IdFieldName, id)
            var pattern = query?.findFirst()

            if (pattern == null && create) {
                pattern = realm.createObject<BolusPattern>(BolusPattern::class.java)
                pattern!!.NLID = id
            }
            patternTask.trySetResult(pattern)
        }, patternTask.task).continueWith { task ->
            if (task.isFaulted) {
                return@continueWith  task
            }
            patternTask.trySetResult(task.result)
        }

        return patternTask.task
    }
}
