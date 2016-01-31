package com.nlefler.glucloser.dataSource.jsonAdapter

import com.nlefler.glucloser.models.Food
import com.nlefler.glucloser.models.Meal
import com.nlefler.glucloser.models.json.MealJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.realm.Realm
import io.realm.RealmList

/**
 * Created by nathan on 1/31/16.
 */
public class MealJsonAdapter(val realm: Realm) {
    @FromJson fun fromJson(json: MealJson): Meal {
        val meal = realm.createObject(Meal::class.java)

        realm.executeTransaction {
            meal.primaryId = json.primaryId
            meal.date = json.date
            meal.bolusPattern = json.bolusPattern
            meal.carbs = json.carbs
            meal.insulin = json.insulin
            meal.beforeSugar = json.beforeSugar
            meal.isCorrection = json.isCorrection
            meal.foods.addAll(json.foods)
            meal.place = json.place
        }

        return meal
    }

    @ToJson fun toJson(meal: Meal): MealJson {
        val json = MealJson(
                primaryId = meal.primaryId,
                date = meal.date,
                bolusPattern = meal.bolusPattern,
                carbs = meal.carbs,
                insulin = meal.insulin,
                beforeSugar = meal.beforeSugar,
                isCorrection = meal.isCorrection,
                foods = meal.foods.toList(),
                place = meal.place
        )
        return json
    }
}
