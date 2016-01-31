package com.nlefler.glucloser.dataSource.jsonAdapter

import com.nlefler.glucloser.models.BolusPattern
import com.nlefler.glucloser.models.json.BolusPatternJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.realm.Realm

/**
 * Created by nathan on 1/31/16.
 */
public class BolusPatternJsonAdapter(val realm: Realm) {
    @FromJson fun fromJson(json: BolusPatternJson): BolusPattern {
        val pattern = realm.createObject(BolusPattern::class.java)

        realm.executeTransaction {
            pattern.primaryId = json.primaryId
            pattern.rateCount = json.rateCount
            pattern.rates.addAll(json.rates)
        }
        return pattern
    }

    @ToJson fun toJson(pattern: BolusPattern): BolusPatternJson {
        val json = BolusPatternJson(
                primaryId = pattern.primaryId,
                rateCount = pattern.rateCount,
                rates = pattern.rates.toList()
        )
        return json
    }
}
