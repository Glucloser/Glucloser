package com.nlefler.glucloser.a.dataSource

import com.nlefler.glucloser.a.dataSource.jsonAdapter.MealJsonAdapter
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.models.*
import com.nlefler.glucloser.a.models.parcelable.MealParcelable
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.requery.kotlin.eq
import io.requery.query.Result
import rx.Observable
import java.util.*

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

    fun save(meal: Meal) {
        dbManager.data.upsert(meal)
    }

    fun parcelableFromMeal(meal: Meal): MealParcelable {
        val parcelable = MealParcelable()
        val place = meal.place
        if (place != null) {
            val placePar = placeFactory.parcelableFromPlace(place)
            if (placePar != null) {
                parcelable.placeParcelable = placePar
            }
        }
        parcelable.carbs = meal.carbs
        parcelable.insulin = meal.insulin
        parcelable.primaryId = meal.primaryId
        parcelable.isCorrection = meal.isCorrection

        val beforeSugar = meal.beforeSugar
        if (beforeSugar != null) {
            parcelable.bloodSugarParcelable = bloodSugarFactory.parcelableFromBloodSugar(beforeSugar)
        }
        parcelable.date = meal.eatenDate

        val bolusPattern = meal.bolusPattern
        if (bolusPattern != null) {
            parcelable.bolusPatternParcelable = bolusPatternFactory.parcelableFromBolusPattern(bolusPattern)
        }
        meal.foods.forEach { food ->
            parcelable.foodParcelables.add(foodFactory.parcelableFromFood(food))
        }

        return parcelable
    }

    fun jsonAdapter(): JsonAdapter<MealEntity> {
        return Moshi.Builder()
                .add(MealJsonAdapter())
                .build()
                .adapter(MealEntity::class.java)
    }

    fun mealFromParcelable(mealParcelable: MealParcelable): Meal {
        val meal = MealEntity()
        meal.primaryId = mealParcelable.primaryId
        meal.carbs = mealParcelable.carbs
        meal.insulin = mealParcelable.insulin
        meal.isCorrection = mealParcelable.isCorrection
        meal.eatenDate = mealParcelable.date

        val bloodSugarParcelable = mealParcelable.bloodSugarParcelable
        if (bloodSugarParcelable != null) {
            meal.beforeSugar = bloodSugarFactory.bloodSugarFromParcelable(bloodSugarParcelable)
        }

        val bolusPatternParcelable = mealParcelable.bolusPatternParcelable
        if (bolusPatternParcelable != null) {
            meal.bolusPattern = bolusPatternFactory.bolusPatternFromParcelable(bolusPatternParcelable)
        }

        val placeParcelable = mealParcelable.placeParcelable
        if (placeParcelable != null) {
            meal.place = placeFactory.placeFromParcelable(placeParcelable)
        }

        val foods = ArrayList<Food>()
        mealParcelable.foodParcelables.forEach { fp -> foods.add(foodFactory.foodFromParcelable(fp)) }
        meal.foods = foods

        return meal
    }

    private fun mealForMealId(id: String): Observable<Result<MealEntity>> {
        if (id.isEmpty()) {
            return Observable.error(Exception("Invalid Id"))
        }
        return dbManager.data.select(MealEntity::class).where(Meal::primaryId.eq(id)).get().toSelfObservable()
    }
}
