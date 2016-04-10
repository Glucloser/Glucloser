package com.nlefler.glucloser.a.dataSource.jsonAdapter

import com.nlefler.glucloser.a.models.*
import com.nlefler.glucloser.a.models.json.MealJson
import com.nlefler.glucloser.a.models.Meal
import com.nlefler.glucloser.a.models.Place
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.realm.Realm
import io.realm.RealmList

/**
 * Created by nathan on 1/31/16.
 */
public class MealJsonAdapter(val realm: Realm) {
    val bolusPatternAdapter = BolusPatternJsonAdapter(realm)
    val sugarAdapter = BloodSugarJsonAdapter(realm)
    val foodAdapter = FoodJsonAdapter(realm)
    val placeAdapter = PlaceJsonAdapter(realm)

    @FromJson fun fromJson(json: MealJson): Meal {
        val meal = realm.createObject(Meal::class.java)

        var place: Place? = null
        if (json.placeId != null) {
            val query = realm.where<Place>(Place::class.java).equalTo(Place.PrimaryKeyName, json.placeId)
            place = query.findFirst()
        }
        realm.executeTransaction {
            meal.primaryId = json.primaryId
            meal.date = json.date
            meal.bolusPattern = if (json.bolusPattern != null) bolusPatternAdapter.fromJson(json.bolusPattern) else null
            meal.carbs = json.carbs
            meal.insulin = json.insulin
            meal.beforeSugar = if (json.beforeSugar != null) sugarAdapter.fromJson(json.beforeSugar) else null
            meal.isCorrection = json.isCorrection
            json.foods.forEach { foodJson ->
                meal.foods.add(foodAdapter.fromJson(foodJson))
            }
            meal.place = place
        }

        return meal
    }

    @ToJson fun toJson(meal: Meal): MealJson {
        val json = MealJson(
                primaryId = meal.primaryId,
                date = meal.date,
                bolusPattern = if (meal.bolusPattern != null) bolusPatternAdapter.toJson(meal.bolusPattern!!) else null,
                carbs = meal.carbs,
                insulin = meal.insulin,
                beforeSugar = if (meal.beforeSugar != null) sugarAdapter.toJson(meal.beforeSugar!!) else null,
                isCorrection = meal.isCorrection,
                foods = meal.foods.map { food -> foodAdapter.toJson(food) },
                placeId = if (meal.place != null) meal.place?.primaryId else null
        )
        return json
    }
}
