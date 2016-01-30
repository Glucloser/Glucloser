package com.nlefler.glucloser.dataSource

import android.content.Context
import android.util.Log
import bolts.Continuation
import bolts.Task
import bolts.TaskCompletionSource
import com.nlefler.glucloser.models.*

import java.util.Date
import java.util.UUID

import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmQuery
import rx.functions.Action1
import rx.functions.Action2
import javax.inject.Inject

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class MealFactory @Inject constructor(val realmManager: RealmManager,
                                             val bolusPatternFactory: BolusPatternFactory,
                                             val bloodSugarFactory: BloodSugarFactory,
                                             val placeFactory: PlaceFactory) {
    private val LOG_TAG = "MealFactory"

    public fun meal(): Task<Meal?> {
        val mealTask = TaskCompletionSource<Meal?>()
        mealForMealId(UUID.randomUUID().toString(), true).continueWith { task ->
            if (task.isFaulted) {
                mealTask.trySetError(task.error)
            }
            else {
                mealTask.trySetResult(task.result)
            }
        }

        return mealTask.task
    }

    public fun fetchMeal(id: String): Task<Meal?> {
        return mealForMealId(id, false).continueWithTask({ task ->
            if (!task.isFaulted) {
                return@continueWithTask Task.forResult<Meal?>(task.result)
            }

            val fetchTask = TaskCompletionSource<Meal?>()

            val parseQuery = ParseQuery.getQuery<ParseObject>(Meal.ParseClassName)
            parseQuery.whereEqualTo(Meal.IdFieldName, id)
            parseQuery.findInBackground({parseObjects: List<ParseObject>, e: ParseException ->
                if (!parseObjects.isEmpty()) {
                    mealFromParseObject(parseObjects.get(0)).continueWith { task ->
                        if (task.isFaulted) {
                            fetchTask.trySetError(task.error)
                        }
                        else {
                            fetchTask.trySetResult(task.result)
                        }
                    }
                } else {
                    fetchTask.trySetResult(null)
                }
            })

            return@continueWithTask fetchTask.task
        })
    }

    public fun parcelableFromMeal(meal: Meal): MealParcelable {
        val parcelable = MealParcelable()
        if (meal.place != null) {
            parcelable.placeParcelable = placeFactory.parcelableFromPlace(meal.place!!)
        }
        parcelable.carbs = meal.carbs
        parcelable.insulin = meal.insulin
        parcelable.id = meal.primaryId
        parcelable.isCorrection = meal.isCorrection
        if (meal.beforeSugar != null) {
            parcelable.bloodSugarParcelable = bloodSugarFactory.parcelableFromBloodSugar(meal.beforeSugar!!)
        }
        parcelable.date = meal.date
        if (meal.bolusPattern != null) {
            parcelable.bolusPatternParcelable = bolusPatternFactory.parcelableFromBolusPattern(meal.bolusPattern!!)
        }

        return parcelable
    }

    public fun mealFromParcelable(parcelable: MealParcelable): Task<Meal?> {
        var placeTask: Task<Place?> = Task.forResult(null)
        if (parcelable.placeParcelable != null) {
            placeTask = placeFactory.placeFromParcelable(parcelable.placeParcelable!!)
        }

        var beforeSugarTask: Task<BloodSugar?>? = Task.forResult(null)
        if (parcelable.bloodSugarParcelable != null) {
            beforeSugarTask = bloodSugarFactory.bloodSugarFromParcelable(parcelable.bloodSugarParcelable!!)
        }

        var bolusPatternTask: Task<BolusPattern?>? = Task.forResult(null)
        if (parcelable.bolusPatternParcelable != null) {
            bolusPatternTask = bolusPatternFactory.bolusPatternFromParcelable(parcelable.bolusPatternParcelable!!)
        }

        val mealTask = mealForMealId(parcelable.id, true)

        val doneTask = TaskCompletionSource<Meal?>()
        Task.whenAll(arrayListOf(beforeSugarTask, bolusPatternTask, placeTask, mealTask)).continueWith { task ->
            if (task.isFaulted) {
                doneTask.trySetError(task.error)
                return@continueWith
            }

            val meal = mealTask.result

            meal?.insulin = parcelable.insulin
            meal?.carbs = parcelable.carbs
            meal?.place = placeTask.result
            meal?.beforeSugar = beforeSugarTask?.result
            meal?.isCorrection = parcelable.isCorrection
            meal?.date = parcelable.date
            meal?.bolusPattern = bolusPatternTask?.result

            doneTask.trySetResult(meal)
        }

        return doneTask.task
    }

    protected fun mealFromParseObject(parseObject: ParseObject?): Task<Meal?> {
        if (parseObject == null) {
            val errorMessage = "Can't create Meal from Parse object, null"
            Log.e(LOG_TAG, errorMessage)
            return Task.forError(Exception(errorMessage))
        }

        val mealId = parseObject.getString(Meal.IdFieldName)
        if (mealId?.length == 0) {
            val errorMessage = "Can't create Meal from Parse object, no id"
            Log.e(LOG_TAG, errorMessage)
            return Task.forError(Exception(errorMessage))
        }

        val beforeSugarTask = bloodSugarFactory.bloodSugarFromParseObject(parseObject.getParseObject(Meal.BeforeSugarFieldName))
        val bolusPatternTask = bolusPatternFactory.bolusPatternFromParseObject(parseObject.getParseObject(Meal.BolusPatternFieldName))
        val placeTask = placeFactory.placeFromParseObject(parseObject.getParseObject(Meal.PlaceFieldName))
        val mealTask = mealForMealId(mealId, true)

        val doneTask = TaskCompletionSource<Meal?>()

        Task.whenAll(arrayListOf(beforeSugarTask, bolusPatternTask, placeTask, mealTask)).continueWith { task ->
            if (task.isFaulted) {
                doneTask.trySetError(task.error)
                return@continueWith
            }

            val meal = mealTask.result
            val beforeSugar = beforeSugarTask.result
            val bolusPattern = bolusPatternTask.result

            val carbs = parseObject.getInt(Meal.CarbsFieldName)
            val insulin = parseObject.getDouble(Meal.InsulinFieldName).toFloat()
            val correction = parseObject.getBoolean(Meal.CorrectionFieldName)
            val mealDate = parseObject.getDate(Meal.MealDateFieldName)

            if (carbs >= 0 && carbs != meal?.carbs) {
                meal?.carbs = carbs
            }
            if (insulin >= 0 && meal?.insulin != insulin) {
                meal?.insulin = insulin
            }
            if (beforeSugar != null && bloodSugarFactory.areBloodSugarsEqual(meal?.beforeSugar, beforeSugar)) {
                meal?.beforeSugar = beforeSugar
            }
            if (meal?.isCorrection != correction) {
                meal?.isCorrection = correction
            }
            if (placeTask.result != null && placeFactory.arePlacesEqual(placeTask.result, meal?.place)) {
                meal?.place = placeTask.result
            }
            if (mealDate != null) {
                meal?.date = mealDate
            }
            meal?.bolusPattern = bolusPattern

            doneTask.trySetResult(meal)
        }

        return doneTask.task
    }

    /**
     * Fetches or creates a ParseObject representing the provided Meal
     * @param meal
     * *
     * @param action Returns the ParseObject, and true if the object was created and should be saved.
     */
    internal fun parseObjectFromMeal(meal: Meal,
                                     beforeSugarObject: ParseObject?,
                                     foodObjects: List<ParseObject>,
                                     placeObject: ParseObject?,
                                     action: Action2<ParseObject?, Boolean>?) {
        if (action == null) {
            Log.e(LOG_TAG, "Unable to create Parse object from Meal, action null")
            return
        }
        if (meal.primaryId.length == 0) {
            Log.e(LOG_TAG, "Unable to create Parse object from Meal, meal null or no id")
            action.call(null, false)
            return
        }

        val parseQuery = ParseQuery.getQuery<ParseObject>(Meal.ParseClassName)
        parseQuery.whereEqualTo(Meal.IdFieldName, meal.primaryId)
        parseQuery.setLimit(1)
        parseQuery.firstInBackground.continueWithTask({ task ->
            if (task.result == null) {
                Task.forResult(Pair(ParseObject(Meal.ParseClassName), true))
            } else {
                Task.forResult(Pair(task.result, false))
            }
        }).continueWith({ task ->
            val resultPair = task.result
            val parseObject = resultPair.first
            val created = resultPair.second
            parseObject.put(Meal.IdFieldName, meal.primaryId)
            if (placeObject != null) {
                parseObject.put(Meal.PlaceFieldName, placeObject)
            }
            if (beforeSugarObject != null) {
                parseObject.put(Meal.BeforeSugarFieldName, beforeSugarObject)
            }
            parseObject.put(Meal.CorrectionFieldName, meal.isCorrection)
            parseObject.put(Meal.CarbsFieldName, meal.carbs)
            parseObject.put(Meal.InsulinFieldName, meal.insulin)
            parseObject.put(Meal.MealDateFieldName, meal.date)
            parseObject.put(Meal.FoodListFieldName, foodObjects)
            if (meal.bolusPattern != null) {
                parseObject.put(Meal.BolusPatternFieldName, bolusPatternFactory.parseObjectFromBolusPattern(meal.bolusPattern!!))
            }
            action.call(parseObject, created)
        })
    }

    private fun mealForMealId(id: String, create: Boolean): Task<Meal?> {
        return realmManager.executeTransaction(object: RealmManager.Tx<Meal?> {
            override fun dependsOn(): List<RealmObject?> {
                return emptyList()
            }

            override fun execute(dependsOn: List<RealmObject?>, realm: Realm): Meal? {
                if (create && id.length == 0) {
                    val meal = realm.createObject<Meal>(Meal::class.java)
                    meal?.primaryId = UUID.randomUUID().toString()
                    meal?.date = Date()
                    return meal
                }

                val query = realm.where<Meal>(Meal::class.java)

                query?.equalTo(Meal.IdFieldName, id)
                var meal = query?.findFirst()

                if (meal == null && create) {
                    meal = realm.createObject<Meal>(Meal::class.java)
                    meal!!.primaryId = id
                    meal.date = Date()
                }

                return meal
            }
        })
    }
}
