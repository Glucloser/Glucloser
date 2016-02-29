package com.nlefler.glucloser.dataSource.jsonAdapter

import android.util.Log
import bolts.Task
import com.nlefler.glucloser.models.Snack
import com.nlefler.glucloser.models.json.SnackJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.realm.Realm

/**
 * Created by nathan on 1/31/16.
 */
public class SnackJsonAdapter(val realm: Realm) {
    val bolusPatternAdapter = BolusPatternJsonAdapter(realm)
    val sugarAdapter = BloodSugarJsonAdapter(realm)
    val foodAdapter = FoodJsonAdapter(realm)

    @FromJson fun fromJson(json: SnackJson): Snack {
        val snack = realm.createObject(Snack::class.java)

        realm.executeTransaction {
            snack.primaryId = json.primaryId

            snack.beforeSugar = if (json.beforeSugar != null) sugarAdapter.fromJson(json.beforeSugar) else null
            snack.carbs = json.carbs
            snack.insulin = json.insulin
            snack.isCorrection = json.isCorrection
            snack.date = json.date
            snack.foods.addAll(json.foods.map { foodJson -> foodAdapter.fromJson(foodJson) })
        }

        return snack
    }

    @ToJson fun toJson(snack: Snack): SnackJson {
        val json = SnackJson(primaryId = snack.primaryId,
                date = snack.date,
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
