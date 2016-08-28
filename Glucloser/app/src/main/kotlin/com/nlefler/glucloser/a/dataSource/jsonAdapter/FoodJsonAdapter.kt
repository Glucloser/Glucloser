package com.nlefler.glucloser.a.dataSource.jsonAdapter

import com.nlefler.glucloser.a.models.Food
import com.nlefler.glucloser.a.models.FoodEntity
import com.nlefler.glucloser.a.models.json.FoodJson
import com.nlefler.glucloser.a.models.parcelable.FoodParcelable
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

/**
 * Created by nathan on 1/31/16.
 */
public class FoodJsonAdapter() {
    @FromJson fun fromJson(json: FoodJson): Food {
        val food = FoodParcelable()
        food.primaryId = json.primaryId
        food.carbs = json.carbs
        food.foodName = json.foodName
        return food
    }

    @ToJson fun toJson(food: Food): FoodJson {
        return FoodJson(
                primaryId = food.primaryId,
                foodName = food.foodName,
                carbs = food.carbs
        )
    }
}
