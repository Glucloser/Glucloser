package com.nlefler.glucloser.a.dataSource

import bolts.Continuation
import com.nlefler.glucloser.a.models.BolusRate

import bolts.Task
import com.nlefler.glucloser.a.dataSource.jsonAdapter.BolusRateJsonAdapter
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.models.parcelable.BolusRateParcelable
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.realm.Realm
import io.realm.RealmObject
import javax.inject.Inject

/**
 * Created by nathan on 9/1/15.
 */
class BolusRateFactory @Inject constructor(val dbManager: DBManager) {

    fun emptyRate(): Task<BolusRate?> {
        return bolusRateForId("__glucloser_special_empty_bolus_rate", true)
    }

    public fun bolusRateFromParcelable(parcelable: BolusRateParcelable): Task<BolusRate?> {
        return bolusRateForId(parcelable.id, true).continueWithTask { task ->
            if (task.isFaulted) {
                return@continueWithTask task
            }
            val rate = task.result
            return@continueWithTask dbManager.executeTransaction(object: DBManager.Tx<BolusRate?> {
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
        return Moshi.Builder()
                .add(BolusRateJsonAdapter(dbManager.defaultRealm()))
                .build()
                .adapter(BolusRate::class.java)
    }

    private fun bolusRateForId(id: String, create: Boolean): Task<BolusRate?> {
        assert(id.length > 0)

        return dbManager.executeTransaction(object: DBManager.Tx<BolusRate?> {
            override fun dependsOn(): List<RealmObject?> {
                return emptyList()
            }

            override fun execute(dependsOn: List<RealmObject?>, realm: Realm): BolusRate? {

                val query = realm.where<BolusRate>(BolusRate::class.java)

                query?.equalTo(BolusRate.IdFieldName, id)
                val foundRate = query?.findFirst()
                if (foundRate != null) {
                    return foundRate
                }
                else if (create) {
                    val rate = realm.createObject<BolusRate>(BolusRate::class.java)
                    rate!!.primaryId = id
                    return rate
                }
                else {
                    return null
                }
            }
        })
    }

}
