package com.nlefler.glucloser.a.dataSource

import bolts.Task
import bolts.TaskCompletionSource
import com.nlefler.glucloser.a.dataSource.jsonAdapter.MealJsonAdapter
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.db.SQLStmts
import com.nlefler.glucloser.a.models.BloodSugar
import com.nlefler.glucloser.a.models.BolusPattern
import com.nlefler.glucloser.a.models.parcelable.MealParcelable
import com.nlefler.glucloser.a.models.Meal
import com.nlefler.glucloser.a.models.Place
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

import java.util.Date
import java.util.UUID

import javax.inject.Inject

/**
 * Created by Nathan Lefler on 12/24/14.
 */
class MealFactory @Inject constructor(val dbManager: DBManager,
                                             val bolusPatternFactory: BolusPatternFactory,
                                             val bloodSugarFactory: BloodSugarFactory,
                                             val placeFactory: PlaceFactory,
                                             val foodFactory: FoodFactory) {
    private val LOG_TAG = "MealFactory"

    fun parcelableFromMeal(meal: Meal): MealParcelable {
        val parcelable = MealParcelable()
        if (meal.place != null) {
            parcelable.placeParcelable = placeFactory.parcelableFromPlace(meal.place)
        }
        parcelable.carbs = meal.carbs
        parcelable.insulin = meal.insulin
        parcelable.id = meal.primaryId
        parcelable.isCorrection = meal.isCorrection
        if (meal.beforeSugar != null) {
            parcelable.bloodSugarParcelable = bloodSugarFactory.parcelableFromBloodSugar(meal.beforeSugar)
        }
        parcelable.date = meal.date
        if (meal.bolusPattern != null) {
            parcelable.bolusPatternParcelable = bolusPatternFactory.parcelableFromBolusPattern(meal.bolusPattern)
        }
        meal.foods.forEach { food ->
            parcelable.foodParcelables.add(foodFactory.parcelableFromFood(food))
        }

        return parcelable
    }

    fun jsonAdapter(): JsonAdapter<Meal> {
        return Moshi.Builder()
                .add(MealJsonAdapter())
                .build()
                .adapter(Meal::class.java)
    }

    fun mealFromParcelable(parcelable: MealParcelable): Meal {
        val pattern = bolusPatternFactory.bolusPatternFromParcelable(parcelable.bolusPatternParcelable)
        val sugar = if (parcelable.bloodSugarParcelable != null) {bloodSugarFactory.bloodSugarFromParcelable(parcelable.bloodSugarParcelable!!)} else {null}
        val foods = parcelable.foodParcelables.map {fp -> foodFactory.foodFromParcelable(fp)}
        val place = placeFactory.placeFromParcelable(parcelable.placeParcelable)
        return Meal(parcelable.id, parcelable.date, pattern,
                parcelable.carbs, parcelable.insulin, sugar,
                parcelable.isCorrection, foods, place)
    }

    private fun mealForMealId(id: String): Task<Meal> {
        if (id.isEmpty()) {
            return Task.forError<Meal>(Exception("Invalid ID"))
        }

        val task = TaskCompletionSource<Meal>()
        val mealQuery = SQLStmts.Meal().forID()

        val beforeSugarQuery = SQLStmts.Meal().BeforeSugarForMeal(id)
        var beforeSugar: BloodSugar? = null
        dbManager.query(beforeSugarQuery, arrayOf(id)) { cursor ->
            if (cursor == null) {
                task.setError(Exception("Unable to read db"))
                return@query
            }
            if (!cursor.moveToFirstld()) {
                task.setError(Exception("No result for id and create not set"))
                return@query
            }
            beforeSugar = BloodSugar(beforeSugarQuery.getID(cursor))
        }
        var meal: Meal? = null
        dbManager.query(mealQuery, arrayOf(id)) { cursor ->
            if (cursor == null) {
                task.setError(Exception("Unable to read db"))
                return@query
            }
            if (!cursor.moveToFirst()) {
                task.setError(Exception("No result for id and create not set"))
                return@query
            }
        }
        return dbManager.executeTransaction(object: DBManager.Tx<Meal?> {
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

                query?.equalTo(Meal.PrimaryKeyName, id)
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
