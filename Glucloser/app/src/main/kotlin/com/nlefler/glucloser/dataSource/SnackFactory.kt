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

import java.util.UUID

import io.realm.Realm
import rx.functions.Action1
import rx.functions.Action2
import javax.inject.Inject

/**
 * Created by Nathan Lefler on 4/25/15.
 */
public class SnackFactory @Inject constructor(val realmManager: RealmManager, val bloodSugarFactory: BloodSugarFactory) {
    private val LOG_TAG = "SnackFactory"

    public fun snack(): Task<Snack?> {
        return snackForSnackId("", true)
    }

    public fun fetchSnack(id: String): Task<Snack?> {
        return snackForSnackId(id, false).continueWithTask(Continuation<Snack?, Task<Snack?>> { task ->
            if (task.isCompleted) {
                return@Continuation task
            }

            val parseQuery = ParseQuery.getQuery<ParseObject>(Snack.ParseClassName)
            parseQuery.whereEqualTo(Snack.SnackIdFieldName, id)
            parseQuery.setLimit(1)
            return@Continuation  parseQuery.firstInBackground.continueWithTask(Continuation<ParseObject?, Task<ParseObject?>> { task ->
                if (task.isFaulted) {
                    return@Continuation Task.forError(task.error)
                }
                return@Continuation task.result?.fetchIfNeededInBackground<ParseObject>()
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
        parcelable.id = snack.id
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
        return Task.whenAll(arrayListOf(beforeSugarTask, snackTask)).continueWithTask(Continuation<Void, Task<Snack?>> { task ->
            if (task.isFaulted) {
                return@Continuation Task.forError(task.error)
            }

            val realmTask = TaskCompletionSource<Snack?>()
            realmManager.executeTransaction(Realm.Transaction { realm ->
                val snack = snackTask.result
                snack?.insulin = parcelable.insulin
                snack?.carbs = parcelable.carbs
                snack?.isCorrection = parcelable.isCorrection
                snack?.beforeSugar = beforeSugarTask?.result
                snack?.date = parcelable.date
                realmTask.trySetResult(snack)
            }, realmTask.task)
        })
    }

    protected fun snackFromParseObject(parseObject: ParseObject?): Task<Snack?> {
        if (parseObject == null) {
            val errorMessage = "Can't create Snack from Parse object, null"
            Log.e(LOG_TAG, errorMessage)
            return Task.forError(Exception(errorMessage))
        }

        val snackId = parseObject.getString(Snack.SnackIdFieldName)
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
        return Task.whenAll(arrayListOf(beforeSugarTask, snackTask)).continueWithTask(Continuation<Void?, Task<Snack?>> { task ->
            if (task.isFaulted) {
                return@Continuation Task.forError(task.error)
            }

            val realmTask = TaskCompletionSource<Snack?>()
            return@Continuation realmManager.executeTransaction(Realm.Transaction { realm ->
                val snack = snackTask.result
                if (carbs >= 0 && carbs != snack?.carbs) {
                    snack?.carbs = carbs
                }
                if (insulin >= 0 && snack?.insulin != insulin) {
                    snack?.insulin = insulin
                }
                if (beforeSugarTask.result != null && bloodSugarFactory.areBloodSugarsEqual(snack?.beforeSugar, beforeSugarTask.result)) {
                    snack?.beforeSugar = beforeSugarTask.result
                }
                if (snack?.isCorrection != correction) {
                    snack?.isCorrection = correction
                }
                if (snackDate != null) {
                    snack?.date = snackDate
                }
            }, realmTask.task)
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
        if (snack?.id?.isEmpty() ?: true) {
            Log.e(LOG_TAG, "Unable to create Parse object from Snack, Snack null or no id")
            action.call(null, false)
            return
        }

        val parseQuery = ParseQuery.getQuery<ParseObject>(Snack.ParseClassName)
        parseQuery.whereEqualTo(Snack.SnackIdFieldName, snack!!.id)
        parseQuery.findInBackground({parseObjects: List<ParseObject>, e: ParseException? ->
            val parseObject: ParseObject
            var created = false
            if (parseObjects.isEmpty()) {
                parseObject = ParseObject(Snack.ParseClassName)
                created = true
            } else {
                parseObject = parseObjects.get(0)
            }
            parseObject.put(Snack.SnackIdFieldName, snack.id)
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
        val realmTask = TaskCompletionSource<Snack?>()
        return realmManager.executeTransaction(Realm.Transaction { realm ->
            if (create && id.isEmpty()) {
                val snack = realm.createObject<Snack>(Snack::class.java)
                snack?.id = UUID.randomUUID().toString()
                realmTask.trySetResult(snack)
                return@Transaction
            }

            val query = realm.where<Snack>(Snack::class.java)

            query?.equalTo(Snack.SnackIdFieldName, id)
            var result: Snack? = query?.findFirst()

            if (result == null && create) {
                result = realm.createObject<Snack>(Snack::class.java)
                result!!.id = id
            }
            else if (result == null) {
                realmTask.trySetError(Exception("No Snack for id ${id}"))
            }
            else {
                realmTask.trySetResult(result)
            }
        }, realmTask.task)
    }
}
