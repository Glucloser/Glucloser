package com.nlefler.glucloser.dataSource

import android.content.Context
import android.util.Log
import bolts.Continuation
import bolts.Task
import bolts.TaskCompletionSource
import com.nlefler.glucloser.dataSource.jsonAdapter.EJsonAdapter
import com.nlefler.glucloser.dataSource.jsonAdapter.MealJsonAdapter
import com.nlefler.glucloser.models.*
import com.nlefler.glucloser.models.parcelable.MealParcelable
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

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
        return mealForMealId(id, false)
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

    public fun jsonAdapter(): JsonAdapter<Meal> {
        return Moshi.Builder()
                .add(MealJsonAdapter(realmManager.defaultRealm()))
                .add(EJsonAdapter())
                .build()
                .adapter(Meal::class.java)
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
