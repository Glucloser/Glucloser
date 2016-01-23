package com.nlefler.glucloser.dataSource

import android.content.Context
import android.util.Log
import bolts.Continuation
import bolts.Task
import bolts.TaskCompletionSource
import com.nlefler.glucloser.models.BloodSugar
import com.nlefler.glucloser.models.BloodSugarParcelable

import com.parse.FindCallback
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery

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
        val dateOK = sugar1.date == sugar2.date

        return valueOk && dateOK
    }

    public fun bloodSugarFromParcelable(parcelable: BloodSugarParcelable): Task<BloodSugar?> {
        return bloodSugarForBloodSugarId(parcelable.id, true)
                .continueWithTask(Continuation<BloodSugar?, Task<BloodSugar?>> { task ->
            if (task.isFaulted) {
                return@Continuation task
            }
            val sugar = task.result
            return@Continuation realmManager.executeTransaction(object: RealmManager.Tx {
                override fun dependsOn(): List<RealmObject?> {
                    return listOf(sugar)
                }

                override fun execute(dependsOn: List<RealmObject?>, realm: Realm): List<RealmObject?> {
                    sugar?.value = parcelable.value
                    sugar?.date = parcelable.date
                    return listOf(sugar)
                }
            }).continueWithTask(Continuation<List<RealmObject?>, Task<BloodSugar?>> { task ->
                if (task.isFaulted) {
                    return@Continuation Task.forError(task.error)
                }
                return@Continuation Task.forResult(task.result.firstOrNull() as BloodSugar?)
            })
        })
    }

    public fun parcelableFromBloodSugar(sugar: BloodSugar): BloodSugarParcelable {
        val parcelable = BloodSugarParcelable()
        parcelable.id = sugar.id
        parcelable.value = sugar.value
        parcelable.date = sugar.date
        return parcelable
    }

    internal fun bloodSugarFromParseObject(parseObject: ParseObject?): Task<BloodSugar?> {
        if (parseObject == null) {
            Log.e(LOG_TAG, "Can't create BloodSugar from Parse object, null")
            return Task.forError(Exception("Can't create BloodSugar from Parse object, null"))
        }
        val sugarId = parseObject.getString(BloodSugar.IdFieldName)
        if (sugarId == null || sugarId.isEmpty()) {
            Log.e(LOG_TAG, "Can't create BloodSugar from Parse object, no id")
        }
        val sugarValue = parseObject.getInt(BloodSugar.ValueFieldName)
        val sugarDate = parseObject.getDate(BloodSugar.DateFieldName)

        return bloodSugarForBloodSugarId(sugarId, true)
                .continueWithTask(Continuation<BloodSugar?, Task<BloodSugar?>> { task ->
            if (task.isFaulted) {
                return@Continuation task
            }

            val sugar = task.result!!
            realmManager.executeTransaction(object: RealmManager.Tx {
                override fun dependsOn(): List<RealmObject?> {
                    return listOf(sugar)
                }

                override fun execute(dependsOn: List<RealmObject?>, realm: Realm): List<RealmObject?> {
                    if (sugarValue >= 0 && sugarValue != sugar.value) {
                        sugar.value = sugarValue
                    }
                    if (sugarDate != null) {
                        sugar.date = sugarDate
                    }
                    return listOf(sugar)
                }
            }).continueWithTask(Continuation<List<RealmObject?>, Task<BloodSugar?>> { task ->
                if (task.isFaulted) {
                    return@Continuation  Task.forError(task.error)
                }
                return@Continuation  Task.forResult(task.result.firstOrNull() as BloodSugar?)
            })
        })
    }

    /**
     * Fetches or creates a ParseObject representing the provided BloodSugar
     * @param bloodSugar
     * *
     * @param action Returns the ParseObject, and true if the object was created and should be saved.
     */
    internal fun parseObjectFromBloodSugar(bloodSugar: BloodSugar, action: Action2<ParseObject?, Boolean>?) {
        if (action == null) {
            Log.e(LOG_TAG, "Unable to create Parse object from BloodSugar, action null")
            return
        }
        if (bloodSugar.id?.isEmpty() ?: true) {
            Log.e(LOG_TAG, "Unable to create Parse object from BloodSugar, blood sugar null or no id")
            action.call(null, false)
            return
        }

        val parseQuery = ParseQuery.getQuery<ParseObject>(BloodSugar.ParseClassName)
        parseQuery.whereEqualTo(BloodSugar.IdFieldName, bloodSugar.id)

        parseQuery.findInBackground({parseObjects: List<ParseObject>, e: ParseException? ->
            val parseObject: ParseObject
            var created = false
            if (parseObjects.isEmpty()) {
                parseObject = ParseObject(BloodSugar.ParseClassName)
                created = true
            } else {
                parseObject = parseObjects.get(0)
            }
            parseObject.put(BloodSugar.IdFieldName, bloodSugar.id)
            parseObject.put(BloodSugar.ValueFieldName, bloodSugar.value)
            parseObject.put(BloodSugar.DateFieldName, bloodSugar.date)
            action.call(parseObject, created)
        })
    }

    private fun bloodSugarForBloodSugarId(id: String, create: Boolean): Task<BloodSugar?> {
        return realmManager.executeTransaction(object: RealmManager.Tx {
            override fun dependsOn(): List<RealmObject?> {
                return emptyList()
            }

            override fun execute(dependsOn: List<RealmObject?>, realm: Realm): List<RealmObject?> {
                if (create && (id == null || id.isEmpty())) {
                    val sugar = realm.createObject<BloodSugar>(BloodSugar::class.java)
                    sugar?.id = UUID.randomUUID().toString()
                    return listOf(sugar)
                }

                val query = realm.where<BloodSugar>(BloodSugar::class.java)

                query?.equalTo(BloodSugar.IdFieldName, id)
                var sugar = query?.findFirst()

                if (sugar == null && create) {
                    sugar = realm.createObject<BloodSugar>(BloodSugar::class.java)
                    sugar!!.id = id
                }
                return listOf(sugar)
            }
        }).continueWithTask(Continuation<List<RealmObject?>, Task<BloodSugar?>> { task ->
            if (task.isFaulted) {
                return@Continuation Task.forError(task.error)
            }
            return@Continuation Task.forResult(task.result.firstOrNull() as BloodSugar?)
        })
    }
}
