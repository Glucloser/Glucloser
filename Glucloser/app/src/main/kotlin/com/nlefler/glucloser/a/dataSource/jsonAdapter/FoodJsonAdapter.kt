package com.nlefler.glucloser.a.dataSource.jsonAdapter

import com.nlefler.glucloser.a.models.Food
import com.nlefler.glucloser.a.models.json.FoodJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

/**
 * Created by nathan on 1/31/16.
 */
public class FoodJsonAdapter() {
    @FromJson fun fromJson(json: FoodJson): Food {
        return Food(json.primaryId, json.carbs, json.foodName)
    }

    @ToJson fun toJson(food: Food): FoodJson {
        return FoodJson(
                primaryId = food.primaryID,
                foodName = food.foodName,
                carbs = food.carbs
        )
    }
}
