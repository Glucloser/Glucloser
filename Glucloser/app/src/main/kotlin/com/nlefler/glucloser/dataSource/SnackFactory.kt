package com.nlefler.glucloser.dataSource

import android.content.Context
import android.util.Log
import bolts.Continuation
import bolts.Task
import bolts.TaskCompletionSource
import com.nlefler.glucloser.models.BloodSugar
import com.nlefler.glucloser.models.Snack
import com.nlefler.glucloser.models.SnackParcelable

import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery

import io.realm.Realm
import io.realm.RealmObject
import rx.functions.Action1
import rx.functions.Action2
import java.util.*
import javax.inject.Inject

/**
 * Created by Nathan Lefler on 4/25/15.
 */
public class SnackFactory @Inject constructor(val realmManager: RealmManager, val bloodSugarFactory: BloodSugarFactory) {
    private val LOG_TAG = "SnackFactory"

    public fun snack(): Task<Snack?> {
        return snackForSnackId(UUID.randomUUID().toString(), true)
    }

    public fun fetchSnack(id: String): Task<Snack?> {
        return snackForSnackId(id, false).continueWithTask(Continuation<Snack?, Task<Snack?>> snackForId@ { task ->
            if (task.isCompleted) {
                return@snackForId task
            }

            val parseQuery = ParseQuery.getQuery<ParseObject>(Snack.ParseClassName)
            parseQuery.whereEqualTo(Snack.IdFieldName, id)
            parseQuery.setLimit(1)
            return@snackForId parseQuery.firstInBackground.continueWithTask(Continuation<ParseObject?, Task<ParseObject?>> parseQuery@ { task ->
                if (task.isFaulted) {
                    return@parseQuery Task.forError(task.error)
                }
                return@parseQuery task.result?.fetchIfNeededInBackground<ParseObject>()
            }).continueWithTask(Continuation<ParseObject?, Task<Snack?>> { task ->
                if (task.isFaulted) {
                    return@Continuation Task.forError<Snack?>(task.error)
                }
                return@Continuation  snackFromParseObject(task.result)
            })
        })

    }

    public fun parcelableFromSnack(snack: Snack): SnackParcelable {
        val parcelable = SnackParcelable()
        parcelable.carbs = snack.carbs
        parcelable.insulin = snack.insulin
        parcelable.id = snack.primaryId
        parcelable.isCorrection = snack.isCorrection
        if (snack.beforeSugar != null) {
            parcelable.bloodSugarParcelable = bloodSugarFactory.parcelableFromBloodSugar(snack.beforeSugar!!)
        }
        parcelable.date = snack.date

        return parcelable
    }

    public fun snackFromParcelable(parcelable: SnackParcelable): Task<Snack?> {

        var beforeSugarTask: Task<BloodSugar?>? = Task.forResult(null)
        if (parcelable.bloodSugarParcelable != null) {
            beforeSugarTask = bloodSugarFactory.bloodSugarFromParcelable(parcelable.bloodSugarParcelable!!)
        }

        val snackTask = snackForSnackId(parcelable.id, true)
        return Task.whenAll(arrayListOf(beforeSugarTask, snackTask))
                .continueWithTask(Continuation<Void, Task<Snack?>> whenAll@ { task ->
            if (task.isFaulted) {
                return@whenAll Task.forError(task.error)
            }

                    val snack = snackTask.result
                    val sugar = beforeSugarTask?.result

            return@whenAll realmManager.executeTransaction(object: RealmManager.Tx<Snack?> {
                override fun dependsOn(): List<RealmObject?> {
                    return listOf(sugar, snack)
                }

                override fun execute(dependsOn: List<RealmObject?>, realm: Realm): Snack? {
                    val liveSugar = dependsOn.first() as BloodSugar?
                    val liveSnack = dependsOn.last() as Snack?
                    liveSnack?.insulin = parcelable.insulin
                    liveSnack?.carbs = parcelable.carbs
                    liveSnack?.isCorrection = parcelable.isCorrection
                    liveSnack?.beforeSugar = liveSugar
                    liveSnack?.date = parcelable.date
                    return liveSnack
                }
            })
        })
    }

    protected fun snackFromParseObject(parseObject: ParseObject?): Task<Snack?> {
        if (parseObject == null) {
            val errorMessage = "Can't create Snack from Parse object, null"
            Log.e(LOG_TAG, errorMessage)
            return Task.forError(Exception(errorMessage))
        }

        val snackId = parseObject.getString(Snack.IdFieldName)
        if (snackId == null || snackId.isEmpty()) {
            val errorMessage = "Can't create Snack from Parse object, no id"
            Log.e(LOG_TAG, errorMessage)
            return Task.forError(Exception(errorMessage))
        }

        val beforeSugarTask = bloodSugarFactory.bloodSugarFromParseObject(parseObject.getParseObject(Snack.BeforeSugarFieldName))

        val carbs = parseObject.getInt(Snack.CarbsFieldName)
        val insulin = parseObject.getDouble(Snack.InsulinFieldName).toFloat()
        val correction = parseObject.getBoolean(Snack.CorrectionFieldName)
        val snackDate = parseObject.getDate(Snack.SnackDateFieldName)

        val snackTask = snackForSnackId(snackId, true)
        return Task.whenAll(arrayListOf(beforeSugarTask, snackTask))
                .continueWithTask(Continuation<Void?, Task<Snack?>> { task ->
            if (task.isFaulted) {
                return@Continuation Task.forError(task.error)
            }

                    val sugar = beforeSugarTask.result
                    val snack = snackTask.result

            return@Continuation realmManager.executeTransaction(object: RealmManager.Tx<Snack?> {
                override fun dependsOn(): List<RealmObject?> {
                    return listOf(sugar, snack)
                }

                override fun execute(dependsOn: List<RealmObject?>, realm: Realm): Snack? {
                    val liveSugar = dependsOn.first() as BloodSugar?
                    val liveSnack = dependsOn.last() as Snack?
                    if (carbs >= 0 && carbs != liveSnack?.carbs) {
                        liveSnack?.carbs = carbs
                    }
                    if (insulin >= 0 && liveSnack?.insulin != insulin) {
                        liveSnack?.insulin = insulin
                    }
                    if (beforeSugarTask.result != null && bloodSugarFactory.areBloodSugarsEqual(liveSnack?.beforeSugar, liveSugar)) {
                        liveSnack?.beforeSugar = sugar
                    }
                    if (liveSnack?.isCorrection != correction) {
                        liveSnack?.isCorrection = correction
                    }
                    if (snackDate != null) {
                        liveSnack?.date = snackDate
                    }
                    return liveSnack
                }
            })
        })
    }

    /**
     * Fetches or creates a ParseObject representing the provided Snack
     * @param snack
     * *
     * @param action Returns the ParseObject, and true if the object was created and should be saved.
     */
    internal fun parseObjectFromSnack(snack: Snack?,
                                      beforeSugarObject: ParseObject?,
                                      foodObjects: List<ParseObject>,
                                      action: Action2<ParseObject?, Boolean>?) {
        if (action == null) {
            Log.e(LOG_TAG, "Unable to create Parse object from Snack, action null")
            return
        }
        if (snack?.primaryId?.isEmpty() ?: true) {
            Log.e(LOG_TAG, "Unable to create Parse object from Snack, Snack null or no id")
            action.call(null, false)
            return
        }

        val parseQuery = ParseQuery.getQuery<ParseObject>(Snack.ParseClassName)
        parseQuery.whereEqualTo(Snack.IdFieldName, snack!!.primaryId)
        parseQuery.findInBackground({parseObjects: List<ParseObject>, e: ParseException? ->
            val parseObject: ParseObject
            var created = false
            if (parseObjects.isEmpty()) {
                parseObject = ParseObject(Snack.ParseClassName)
                created = true
            } else {
                parseObject = parseObjects.get(0)
            }
            parseObject.put(Snack.IdFieldName, snack.primaryId)
            if (beforeSugarObject != null) {
                parseObject.put(Snack.BeforeSugarFieldName, beforeSugarObject)
            }
            parseObject.put(Snack.CorrectionFieldName, snack.isCorrection)
            parseObject.put(Snack.CarbsFieldName, snack.carbs)
            parseObject.put(Snack.InsulinFieldName, snack.insulin)
            parseObject.put(Snack.SnackDateFieldName, snack.date)
            parseObject.put(Snack.FoodListFieldName, foodObjects)
            action.call(parseObject, created)
        })
    }

    private fun snackForSnackId(id: String, create: Boolean): Task<Snack?> {
        return realmManager.executeTransaction(object: RealmManager.Tx<Snack?> {
            override fun dependsOn(): List<RealmObject?> {
                return emptyList()
            }

            override fun execute(dependsOn: List<RealmObject?>, realm: Realm): Snack? {
                if (create && id.isEmpty()) {
                    val snack = realm.createObject<Snack>(Snack::class.java)
                    snack?.primaryId = UUID.randomUUID().toString()
                    snack?.date = Date()
                    return snack
                }

                val query = realm.where<Snack>(Snack::class.java)

                query?.equalTo(Snack.IdFieldName, id)
                var snack: Snack? = query?.findFirst()

                if (snack == null && create) {
                    snack = realm.createObject<Snack>(Snack::class.java)
                    snack?.primaryId = id
                    snack?.date = Date()
                }
                return snack
            }
        })
    }
}
