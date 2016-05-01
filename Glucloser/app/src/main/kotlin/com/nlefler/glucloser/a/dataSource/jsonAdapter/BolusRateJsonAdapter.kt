package com.nlefler.glucloser.a.dataSource.jsonAdapter

import com.nlefler.glucloser.a.models.BolusRate
import com.nlefler.glucloser.a.models.json.BolusRateJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

/**
 * Created by nathan on 1/31/16.
 */
public class BolusRateJsonAdapter() {
    @FromJson fun fromJson(json: BolusRateJson): BolusRate {
        return BolusRate(json.primaryId, json.ordinal, json.startTime, json.carbsPerUnit)
    }

    @ToJson fun toJson(rate: BolusRate): BolusRateJson {
        return BolusRateJson(
                primaryId = rate.primaryId,
                ordinal = rate.ordinal,
                startTime = rate.startTime,
                carbsPerUnit = rate.carbsPerUnit,
                unit = ""
        )
    }
}
