package com.nlefler.glucloser.a.dataSource.jsonAdapter

import com.nlefler.glucloser.a.models.json.MealJson
import com.nlefler.glucloser.a.models.Meal
import com.nlefler.glucloser.a.models.MealEntity
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

        val place = if (json.place != null) placeAdapter.fromJson(json.place) else null
        val foods = json.foods.map { foodJson -> foodAdapter.fromJson(foodJson)}
        val bolusPattern = if (json.bolusPattern != null) bolusPatternAdapter.fromJson(json.bolusPattern) else null
        val beforeSugar = if (json.beforeSugar != null) {sugarAdapter.fromJson(json.beforeSugar)} else {null}

        val meal = MealEntity()
        meal.primaryId = json.primaryId
        meal.eatenDate = json.date
        meal.bolusPattern = bolusPattern
        meal.carbs = json.carbs
        meal.insulin = json.insulin
        meal.beforeSugar = beforeSugar
        meal.isCorrection = json.isCorrection
        meal.foods = foods
        meal.place = place
        return meal
    }

    @ToJson fun toJson(meal: Meal): MealJson {
        val pattern = meal.bolusPattern
        val sugar = meal.beforeSugar
        val place = meal.place
        return MealJson(
                primaryId = meal.primaryId,
                date = meal.eatenDate,
                bolusPattern = if (pattern != null) bolusPatternAdapter.toJson(pattern) else  null ,
                carbs = meal.carbs,
                insulin = meal.insulin,
                beforeSugar = if (sugar != null) sugarAdapter.toJson(sugar) else null,
                isCorrection = meal.isCorrection,
                foods = meal.foods.map { food -> foodAdapter.toJson(food) },
                place = if (place != null) placeAdapter.toJson(place) else null
        )
    }
}
