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
    companion object {
        private val LOG_TAG = "SnackJsonAdapter"
    }

    @FromJson fun fromJson(json: SnackJson): Snack {
        val snack = realm.createObject(Snack::class.java)

        realm.executeTransaction {
            snack.primaryId = json.primaryId

            snack.beforeSugar = json.beforeSugar
            snack.carbs = json.carbs
            snack.insulin = json.insulin
            snack.isCorrection = json.isCorrection
            snack.date = json.date
        }

        return snack
    }

    @ToJson fun toJson(snack: Snack): SnackJson {
        val json = SnackJson(primaryId = snack.primaryId,
                date = snack.date,
                bolusPattern = snack.bolusPattern,
                carbs = snack.carbs,
                insulin = snack.insulin,
                beforeSugar = snack.beforeSugar,
                isCorrection = snack.isCorrection,
                foods = snack.foods.toList()
                )
        return json
    }
}
