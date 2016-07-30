package com.nlefler.glucloser.a.dataSource

import com.nlefler.glucloser.a.dataSource.jsonAdapter.MealJsonAdapter
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.models.BloodSugar
import com.nlefler.glucloser.a.models.BolusPattern
import com.nlefler.glucloser.a.models.parcelable.MealParcelable
import com.nlefler.glucloser.a.models.Meal
import com.nlefler.glucloser.a.models.Place
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.requery.kotlin.eq
import io.requery.query.Result
import rx.Observable

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

    fun jsonAdapter(): JsonAdapter<Meal> {
        return Moshi.Builder()
                .add(MealJsonAdapter())
                .build()
                .adapter(Meal::class.java)
    }

    fun mealFromParcelable(parcelable: MealParcelable): Meal {
        val patternPar = parcelable.bolusPatternParcelable
        val pattern = if (patternPar != null) {bolusPatternFactory.bolusPatternFromParcelable(patternPar)} else null
        val sugar = if (parcelable.bloodSugarParcelable != null) {bloodSugarFactory.bloodSugarFromParcelable(parcelable.bloodSugarParcelable!!)} else {null}
        val foods = parcelable.foodParcelables.map {fp -> foodFactory.foodFromParcelable(fp)}
        val placePar = parcelable.placeParcelable
        val place = if (placePar != null) placeFactory.placeFromParcelable(placePar) else null
        return Meal(parcelable.primaryId, parcelable.date, pattern,
                parcelable.carbs, parcelable.insulin, sugar,
                parcelable.isCorrection, foods, place)
    }

    private fun mealForMealId(id: String): Observable<Result<Meal>> {
        if (id.isEmpty()) {
            return Observable.error(Exception("Invalid Id"))
        }
        return dbManager.data.select(Meal::class).where(Meal::primaryId.eq(id)).get().toSelfObservable()
    }
}
