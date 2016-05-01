package com.nlefler.glucloser.a.dataSource.jsonAdapter

import com.nlefler.glucloser.a.models.BolusPattern
import com.nlefler.glucloser.a.models.json.BolusPatternJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

/**
 * Created by nathan on 1/31/16.
 */
public class BolusPatternJsonAdapter() {
    val rateAdapter = BolusRateJsonAdapter()

    @FromJson fun fromJson(json: BolusPatternJson): BolusPattern {
        return BolusPattern(primaryId = json.primaryId, rates = json.rates.map { rateJson -> rateAdapter.fromJson(rateJson) })
    }

    @ToJson fun toJson(pattern: BolusPattern): BolusPatternJson {
        val rates = pattern.rates.map { rate ->
           return@map rateAdapter.toJson(rate)
        }
        return BolusPatternJson(
                primaryId = pattern.primaryId,
                rates = rates
        )
    }
}
