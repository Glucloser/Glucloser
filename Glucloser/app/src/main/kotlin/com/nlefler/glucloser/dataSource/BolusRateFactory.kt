package com.nlefler.glucloser.dataSource

import bolts.Continuation
import com.nlefler.glucloser.models.BolusRate

import bolts.Task
import bolts.TaskCompletionSource
import com.nlefler.glucloser.dataSource.jsonAdapter.BolusRateJsonAdapter
import com.nlefler.glucloser.models.parcelable.BolusRateParcelable
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
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

    public fun jsonAdapter(): JsonAdapter<BolusRate> {
        return Moshi.Builder().add(BolusRateJsonAdapter(realmManager.defaultRealm())).build().adapter(BolusRate::class.java)
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
