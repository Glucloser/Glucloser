package com.nlefler.glucloser.a.dataSource.jsonAdapter

import com.nlefler.glucloser.a.models.*
import com.nlefler.glucloser.a.models.json.SnackJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.*

/**
 * Created by nathan on 1/31/16.
 */
class SnackJsonAdapter() {
    val bolusPatternAdapter = BolusPatternJsonAdapter()
    val sugarAdapter = BloodSugarJsonAdapter()
    val foodAdapter = FoodJsonAdapter()

    @FromJson fun fromJson(json: SnackJson): Snack {
        var bolusPattern: BolusPattern? = null
        if (json.bolusPattern != null) {
            bolusPattern = bolusPatternAdapter.fromJson(json.bolusPattern)
        }
        var beforeSugar: BloodSugar? = null
        if (json.beforeSugar != null) {
            beforeSugar = sugarAdapter.fromJson(json.beforeSugar)
        }
        var foods = ArrayList<Food>()
        json.foods.forEach { jf ->
            foods.add(foodAdapter.fromJson(jf))
        }

        val snack = SnackEntity()
        snack.primaryId = json.primaryId
        snack.eatenDate = json.date
        snack.bolusPattern = bolusPattern
        snack.carbs = json.carbs
        snack.insulin = json.insulin
        snack.beforeSugar = beforeSugar
        snack.isCorrection = json.isCorrection
        snack.foods = foods
        return snack
    }

    @ToJson fun toJson(snack: Snack): SnackJson {
        val json = SnackJson(primaryId = snack.primaryId,
                date = snack.eatenDate,
                bolusPattern = if (snack.bolusPattern != null) bolusPatternAdapter.toJson(snack.bolusPattern!!) else null,
                carbs = snack.carbs,
                insulin = snack.insulin,
                beforeSugar = if (snack.beforeSugar != null) sugarAdapter.toJson(snack.beforeSugar!!) else null,
                isCorrection = snack.isCorrection,
                foods = snack.foods.map { food -> foodAdapter.toJson(food) }
        )
        return json
    }
}
