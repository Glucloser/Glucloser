package com.nlefler.glucloser.dataSource.jsonAdapter

import com.nlefler.glucloser.models.BolusRate
import com.nlefler.glucloser.models.json.BolusRateJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.realm.Realm

/**
 * Created by nathan on 1/31/16.
 */
public class BolusRateJsonAdapter(val realm: Realm) {
    @FromJson fun fromJson(json: BolusRateJson): BolusRate {
        val rate = realm.createObject(BolusRate::class.java)

        realm.executeTransaction {
            rate.primaryId = json.primaryId
            rate.ordinal = json.ordinal
            rate.startTime = json.startTime
            rate.carbsPerUnit = json.carbsPerUnit
        }

        return rate
    }

    @ToJson fun toJson(rate: BolusRate): BolusRateJson {
        val json = BolusRateJson(
                primaryId = rate.primaryId,
                ordinal = rate.ordinal,
                startTime = rate.startTime,
                carbsPerUnit = rate.carbsPerUnit
        )
        return json
    }
}
