package com.nlefler.glucloser.a.dataSource

import android.os.Parcelable
import bolts.Continuation
import bolts.Task
import bolts.TaskCompletionSource
import com.nlefler.glucloser.a.dataSource.jsonAdapter.BolusPatternJsonAdapter
import com.nlefler.glucloser.a.dataSource.jsonAdapter.EJsonAdapter
import com.nlefler.glucloser.a.models.BolusPattern
import com.nlefler.glucloser.a.models.parcelable.BolusPatternParcelable
import com.nlefler.glucloser.a.models.BolusRate
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import java.util.*
import javax.inject.Inject

/**
 * Created by nathan on 9/19/15.
 */
class BolusPatternFactory @Inject constructor(val realmManager: RealmManager, val bolusRateFactory: BolusRateFactory) {

    fun emptyPattern(): Task<BolusPattern?> {
        return bolusPatternForId("__glucloser_special_empty_bolus_pattern", false).continueWithTask { task ->
            if (task.isFaulted) {
                return@continueWithTask task
            }
            val pattern = task.result
            if (pattern != null) {
                return@continueWithTask Task.forResult(pattern)
            }

            return@continueWithTask bolusRateFactory.emptyRate().continueWithTask(Continuation<BolusRate?, Task<BolusPattern?>> emptyRate@ { rateTask ->
                if (rateTask.isFaulted) {
                    return@emptyRate Task.forError(Exception("Unable to create BolusRate"))
                } else {
                    val bolusRate = rateTask.result
                    return@emptyRate bolusPatternForId("__glucloser_special_empty_bolus_pattern", true)
                            .continueWithTask(Continuation<BolusPattern?, Task<BolusPattern?>> patternForId@ { task ->
                                val bolusPattern = task.result
                                return@patternForId realmManager.executeTransaction(object : RealmManager.Tx<BolusPattern?> {
                                    override fun dependsOn(): List<RealmObject?> {
                                        return listOf(bolusRate, bolusPattern)
                                    }

                                    override fun execute(dependsOn: List<RealmObject?>, realm: Realm): BolusPattern? {
                                        val liveRate = dependsOn.first() as BolusRate?
                                        val livePattern = dependsOn.last() as BolusPattern?
                                        livePattern?.rates?.add(liveRate)
                                        return livePattern
                                    }
                                })
                            })
                }
            })
        }
    }

    fun parcelableFromBolusPattern(pattern: BolusPattern): BolusPatternParcelable {
        val parcel = BolusPatternParcelable()
        for (rate in pattern.rates) {
            parcel.rates.add(bolusRateFactory.parcelableFromBolusRate(rate))
        }
        return parcel
    }

    fun jsonAdapter(): JsonAdapter<BolusPattern> {
        return Moshi.Builder()
                .add(BolusPatternJsonAdapter(realmManager.defaultRealm()))
                .add(EJsonAdapter())
                .build()
                .adapter(BolusPattern::class.java)
    }

    fun bolusPatternFromParcelable(parcelable: BolusPatternParcelable): Task<BolusPattern?> {
        val patternTask = TaskCompletionSource<BolusPattern?>()

        val id = parcelable.id
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
            pattern?.rates?.addAll(rates)

            patternTask.trySetResult(pattern)
        }

        return patternTask.task
    }

    private fun bolusPatternForId(id: String, create: Boolean): Task<BolusPattern?> {
        return realmManager.executeTransaction(object: RealmManager.Tx<BolusPattern?> {
            override fun dependsOn(): List<RealmObject?> {
                return emptyList()
            }

            override fun execute(dependsOn: List<RealmObject?>, realm: Realm): BolusPattern? {
                if (create && id.length == 0) {
                    val pattern = realm.createObject<BolusPattern>(BolusPattern::class.java)
                    return pattern
                }

                val query = realm.where<BolusPattern>(BolusPattern::class.java)

                query?.equalTo(BolusPattern.IdFieldName, id)
                var pattern = query?.findFirst()

                if (pattern == null && create) {
                    pattern = realm.createObject<BolusPattern>(BolusPattern::class.java)
                    pattern!!.primaryId = id
                }
                return pattern
            }
        })
    }
}
