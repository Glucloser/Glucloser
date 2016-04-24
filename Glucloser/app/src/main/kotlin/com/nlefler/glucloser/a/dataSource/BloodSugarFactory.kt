package com.nlefler.glucloser.a.dataSource

import android.content.Context
import android.util.Log
import bolts.Continuation
import bolts.Task
import bolts.TaskCompletionSource
import com.nlefler.glucloser.a.dataSource.jsonAdapter.BloodSugarJsonAdapter
import com.nlefler.glucloser.a.models.BloodSugar
import com.nlefler.glucloser.a.models.parcelable.BloodSugarParcelable
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

import java.util.Date
import java.util.UUID

import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import rx.functions.Action2
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
        val dateOK = sugar1.recordedDate == sugar2.recordedDate

        return valueOk && dateOK
    }

    public fun bloodSugarFromParcelable(parcelable: BloodSugarParcelable): Task<BloodSugar?> {
        return bloodSugarForBloodSugarId(parcelable.id, true)
                .continueWithTask(Continuation<BloodSugar?, Task<BloodSugar?>> { task ->
                    if (task.isFaulted) {
                        return@Continuation task
                    }
                    val sugar = task.result
                    return@Continuation realmManager.executeTransaction(object : RealmManager.Tx<BloodSugar?> {
                        override fun dependsOn(): List<RealmObject?> {
                            return listOf(sugar)
                        }

                        override fun execute(dependsOn: List<RealmObject?>, realm: Realm): BloodSugar? {
                            val liveSugar = dependsOn.first() as BloodSugar?
                            liveSugar?.value = parcelable.value
                            liveSugar?.recordedDate = parcelable.date
                            return liveSugar
                        }
                    })
                })
    }

    public fun parcelableFromBloodSugar(sugar: BloodSugar): BloodSugarParcelable {
        val parcelable = BloodSugarParcelable()
        parcelable.id = sugar.primaryId
        parcelable.value = sugar.value
        parcelable.date = sugar.recordedDate
        return parcelable
    }

    public fun jsonAdapter(): JsonAdapter<BloodSugar> {
        return Moshi.Builder()
                .add(BloodSugarJsonAdapter(realmManager.defaultRealm()))
                .build()
                .adapter(BloodSugar::class.java)
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
                    sugar?.recordedDate = Date()
                    sugar?.value = 0
                    return sugar
                }

                val query = realm.where<BloodSugar>(BloodSugar::class.java)

                query?.equalTo(BloodSugar.IdFieldName, id)
                val foundSugar: BloodSugar? = query?.findFirst()
                if (foundSugar != null) {
                    return foundSugar;
                }
                else if (create) {
                    val sugar = realm.createObject<BloodSugar>(BloodSugar::class.java)
                    sugar.primaryId = id
                    sugar.recordedDate = Date()
                    sugar.value = 0
                    return sugar
                }
                return null
            }
        })
    }
}
