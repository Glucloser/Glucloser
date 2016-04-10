package com.nlefler.glucloser.a.dataSource.jsonAdapter

import com.nlefler.glucloser.a.models.Food
import com.nlefler.glucloser.a.models.json.FoodJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.realm.Realm

/**
 * Created by nathan on 1/31/16.
 */
public class FoodJsonAdapter(val realm: Realm) {
    @FromJson fun fromJson(json: FoodJson): Food {
        val food = realm.createObject(Food::class.java)

        realm.executeTransaction {
            food.primaryId = json.primaryId
            food.foodName = json.foodName
            food.carbs = json.carbs
        }

        return food
    }

    @ToJson fun toJson(food: Food): FoodJson {
        val json = FoodJson(
                primaryId = food.primaryId,
                foodName = food.foodName,
                carbs = food.carbs
        )
        return json
    }
}
