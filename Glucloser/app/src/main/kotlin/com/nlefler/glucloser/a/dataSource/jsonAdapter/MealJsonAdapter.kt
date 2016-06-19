package com.nlefler.glucloser.a.dataSource.jsonAdapter

import com.nlefler.glucloser.a.models.json.MealJson
import com.nlefler.glucloser.a.models.Meal
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

/**
 * Created by nathan on 1/31/16.
 */
class MealJsonAdapter() {
    val bolusPatternAdapter = BolusPatternJsonAdapter()
    val sugarAdapter = BloodSugarJsonAdapter()
    val foodAdapter = FoodJsonAdapter()
    val placeAdapter = PlaceJsonAdapter()

    @FromJson fun fromJson(json: MealJson): Meal {

        val place = placeAdapter.fromJson(json.place)
        val foods = json.foods.map { foodJson -> foodAdapter.fromJson(foodJson)}
        val bolusPattern = bolusPatternAdapter.fromJson(json.bolusPattern)
        val beforeSugar = if (json.beforeSugar != null) {sugarAdapter.fromJson(json.beforeSugar)} else {null}
        return Meal(json.primaryId, json.date, bolusPattern, json.carbs, json.insulin,
                beforeSugar, json.isCorrection, foods, place)
    }

    @ToJson fun toJson(meal: Meal): MealJson {
        return MealJson(
                primaryId = meal.primaryId,
                date = meal.date,
                bolusPattern = bolusPatternAdapter.toJson(meal.bolusPattern),
                carbs = meal.carbs,
                insulin = meal.insulin,
                beforeSugar = if (meal.beforeSugar != null) sugarAdapter.toJson(meal.beforeSugar) else null,
                isCorrection = meal.isCorrection,
                foods = meal.foods.map { food -> foodAdapter.toJson(food) },
                place = placeAdapter.toJson(meal.place)
        )
    }
}
