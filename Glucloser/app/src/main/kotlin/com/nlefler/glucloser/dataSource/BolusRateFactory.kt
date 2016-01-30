package com.nlefler.glucloser.dataSource

import bolts.Continuation
import com.nlefler.glucloser.models.BolusRate

import bolts.Task
import bolts.TaskCompletionSource
import com.nlefler.glucloser.models.BolusRateParcelable
import io.realm.Realm
import io.realm.RealmObject
import java.util.*
import javax.inject.Inject

/**
 * Created by nathan on 9/1/15.
 */
public class BolusRateFactory @Inject constructor(val realmManager: RealmManager) {

    public fun emptyRate(): Task<BolusRate?> {
        return bolusRateForId("__glucloser_special_empty_bolus_rate", true)
                .continueWithTask { task ->
                    if (task.isFaulted) {
                        return@continueWithTask  task
                    }
                    val bolusRate = task.result
            return@continueWithTask realmManager.executeTransaction(object: RealmManager.Tx<BolusRate?> {
                override fun dependsOn(): List<RealmObject?> {
                    return listOf(bolusRate)
                }

                override fun execute(dependsOn: List<RealmObject?>, realm: Realm): BolusRate? {
                    val liveRate = dependsOn.first() as BolusRate?
                    liveRate?.ordinal = 0
                    liveRate?.carbsPerUnit = 0
                    liveRate?.startTime = 0
                    return liveRate
                }
            })
        }
    }

    public fun bolusRateFromParcelable(parcelable: BolusRateParcelable): Task<BolusRate?> {
        return bolusRateForId("", true).continueWithTask { task ->
            if (task.isFaulted) {
                return@continueWithTask task
            }
            val rate = task.result
            return@continueWithTask realmManager.executeTransaction(object: RealmManager.Tx<BolusRate?> {
                override fun dependsOn(): List<RealmObject?> {
                    return listOf(rate)
                }

                override fun execute(dependsOn: List<RealmObject?>, realm: Realm): BolusRate? {
                    val liveRate = dependsOn.first() as BolusRate?
                    liveRate?.ordinal = parcelable.ordinal
                    liveRate?.carbsPerUnit = parcelable.carbsPerUnit
                    liveRate?.startTime = parcelable.startTime
                    return liveRate
                }
            })
        }
    }

    public fun parcelableFromBolusRate(rate: BolusRate): BolusRateParcelable {
        val parcel = BolusRateParcelable()
        parcel.ordinal = rate.ordinal
        parcel.carbsPerUnit = rate.carbsPerUnit
        parcel.startTime = rate.startTime

        return parcel
    }

    public fun bolusRateFromParseObject(parseObj: ParseObject): Task<BolusRate?> {
        if (!parseObj.getClassName().equals(BolusRate.ParseClassName)) {
            return Task.forError(Exception("Invalid ParseObject"));
        }
        val patternId = parseObj.getString(BolusRate.IdFieldName) ?: return Task.forError(Exception("Invalid ParseObject"))

        return bolusRateForId(patternId, true).continueWithTask { task ->
            if (task.isFaulted) {
                return@continueWithTask task
            }
            val rate = task.result
            return@continueWithTask realmManager.executeTransaction(object: RealmManager.Tx<BolusRate?> {
                override fun dependsOn(): List<RealmObject?> {
                    return listOf(rate)
                }

                override fun execute(dependsOn: List<RealmObject?>, realm: Realm): BolusRate? {
                    val liveRate = dependsOn.first() as BolusRate?
                    liveRate?.ordinal = parseObj.getInt(BolusRate.OridnalFieldName)
                    liveRate?.carbsPerUnit = parseObj.getInt(BolusRate.CarbsPerUnitFieldName)
                    liveRate?.startTime = parseObj.getInt(BolusRate.StartTimeFieldName)
                    return liveRate
                }
            })
        }
    }

    public fun parseObjectFromBolusRate(rate: BolusRate): ParseObject {
        val prs = ParseObject.create(BolusRate.ParseClassName)
        prs.put(BolusRate.OridnalFieldName, rate.ordinal)
        prs.put(BolusRate.CarbsPerUnitFieldName, rate.carbsPerUnit)
        prs.put(BolusRate.StartTimeFieldName, rate.startTime)

        return prs
    }

    private fun bolusRateForId(id: String, create: Boolean): Task<BolusRate?> {
        return realmManager.executeTransaction(object: RealmManager.Tx<BolusRate?> {
            override fun dependsOn(): List<RealmObject?> {
                return emptyList()
            }

            override fun execute(dependsOn: List<RealmObject?>, realm: Realm): BolusRate? {
                if (create && id.length == 0) {
                    val rate = realm.createObject<BolusRate>(BolusRate::class.java)
                    return rate
                }

                val query = realm.where<BolusRate>(BolusRate::class.java)

                query?.equalTo(BolusRate.IdFieldName, id)
                var rate = query?.findFirst()

                if (rate == null && create) {
                    rate = realm.createObject<BolusRate>(BolusRate::class.java)
                    rate!!.primaryId = id
                }
                return rate
            }
        })
    }

}
