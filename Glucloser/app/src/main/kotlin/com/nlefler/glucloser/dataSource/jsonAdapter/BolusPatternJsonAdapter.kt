package com.nlefler.glucloser.dataSource.jsonAdapter

import com.nlefler.glucloser.dataSource.BolusRateFactory
import com.nlefler.glucloser.models.BolusPattern
import com.nlefler.glucloser.models.BolusRate
import com.nlefler.glucloser.models.json.BolusPatternJson
import com.nlefler.glucloser.models.json.BolusRateJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.realm.Realm

/**
 * Created by nathan on 1/31/16.
 */
public class BolusPatternJsonAdapter(val realm: Realm) {
    val rateAdapter = BolusRateJsonAdapter(realm)

    @FromJson fun fromJson(json: BolusPatternJson): BolusPattern {
        val pattern = realm.createObject(BolusPattern::class.java)

        realm.executeTransaction {
            pattern.primaryId = json.primaryId
            pattern.rateCount = json.rateCount
            json.rates.forEach { rateJson ->
                val rate = rateAdapter.fromJson(rateJson)
                pattern.rates.add(rate)
            }
        }
        return pattern
    }

    @ToJson fun toJson(pattern: BolusPattern): BolusPatternJson {
        val rates = pattern.rates.map { rate ->
           return@map rateAdapter.toJson(rate)
        }
        val json = BolusPatternJson(
                primaryId = pattern.primaryId,
                rateCount = pattern.rateCount,
                rates = rates
        )
        return json
    }
}
