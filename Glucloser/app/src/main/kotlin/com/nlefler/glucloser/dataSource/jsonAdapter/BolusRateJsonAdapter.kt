package com.nlefler.glucloser.dataSource.jsonAdapter

import android.util.Log
import com.nlefler.glucloser.models.BolusRate
import com.nlefler.glucloser.models.json.BolusRateJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.realm.Realm
import java.util.*

/**
 * Created by nathan on 1/31/16.
 */
public class BolusRateJsonAdapter(val realm: Realm) {
    @FromJson fun fromJson(json: BolusRateJson): BolusRate {
        realm.beginTransaction()
        val rate: BolusRate = realm.createObject(BolusRate::class.java)
        rate.primaryId = UUID.randomUUID().toString()
        rate.ordinal = json.ordinal
        rate.startTime = json.startTime
        rate.carbsPerUnit = json.carbsPerUnit
        realm.commitTransaction()

        return rate
    }

    @ToJson fun toJson(rate: BolusRate): BolusRateJson {
        val json = BolusRateJson(
                ordinal = rate.ordinal,
                startTime = rate.startTime,
                carbsPerUnit = rate.carbsPerUnit,
                unit = ""
        )
        return json
    }
}
