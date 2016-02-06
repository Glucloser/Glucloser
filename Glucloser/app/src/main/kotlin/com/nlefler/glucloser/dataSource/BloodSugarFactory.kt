package com.nlefler.glucloser.dataSource

import bolts.Task
import com.nlefler.glucloser.dataSource.jsonAdapter.BloodSugarJsonAdapter
import com.nlefler.glucloser.models.BloodSugar
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

import java.util.UUID

import io.realm.Realm
import io.realm.RealmObject
import javax.inject.Inject

/**
 * Created by Nathan Lefler on 1/4/15.
 */
public class BloodSugarFactory @Inject constructor(val realmManager: RealmManager) {
    private val LOG_TAG = "BloodSugarFactory"

    public fun bloodSugar(): Task<BloodSugar?> {
        return bloodSugarForBloodSugarId(UUID.randomUUID().toString(), true)
    }

    public fun areBloodSugarsEqual(sugar1: BloodSugar?, sugar2: BloodSugar?): Boolean {
        if (sugar1 == null || sugar2 == null) {
            return false
        }

        val valueOk = sugar1.value == sugar2.value
        val dateOK = sugar1.date == sugar2.date

        return valueOk && dateOK
    }

    public fun jsonAdapter(): JsonAdapter<BloodSugar> {
        return Moshi.Builder().add(BloodSugarJsonAdapter(realmManager.defaultRealm())).build().adapter(BloodSugar::class.java)
    }

    private fun bloodSugarForBloodSugarId(id: String, create: Boolean): Task<BloodSugar?> {
        return realmManager.executeTransaction(object: RealmManager.Tx<BloodSugar?> {
            override fun dependsOn(): List<RealmObject?> {
                return emptyList()
            }

            override fun execute(dependsOn: List<RealmObject?>, realm: Realm): BloodSugar? {
                if (create && id.isEmpty()) {
                    val sugar = realm.createObject<BloodSugar>(BloodSugar::class.java)
                    sugar?.primaryId = UUID.randomUUID().toString()
                    return sugar
                }

                val query = realm.where<BloodSugar>(BloodSugar::class.java)

                query?.equalTo(BloodSugar.IdFieldName, id)
                var sugar = query?.findFirst()

                if (sugar == null && create) {
                    sugar = realm.createObject<BloodSugar>(BloodSugar::class.java)
                    sugar!!.primaryId = id
                }
                return sugar
            }
        })
    }
}
