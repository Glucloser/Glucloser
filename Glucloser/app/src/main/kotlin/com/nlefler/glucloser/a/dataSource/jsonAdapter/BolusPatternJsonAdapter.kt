package com.nlefler.glucloser.a.dataSource.jsonAdapter

import com.nlefler.glucloser.a.dataSource.BolusRateFactory
import com.nlefler.glucloser.a.models.BolusPattern
import com.nlefler.glucloser.a.models.BolusPatternEntity
import com.nlefler.glucloser.a.models.json.BolusPatternJson
import com.nlefler.glucloser.a.models.parcelable.BolusPatternParcelable
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import javax.inject.Inject

/**
 * Created by nathan on 1/31/16.
 */
class BolusPatternJsonAdapter @Inject constructor(val bolusRateFactory: BolusRateFactory,
                                                  val bolusRateJsonAdapter: BolusRateJsonAdapter) {

    @FromJson fun fromJson(json: BolusPatternJson): BolusPattern {
        val bp = BolusPatternParcelable(bolusRateFactory)
        bp.primaryId = json.primaryId
        bp.rates = json.rates.map { rateJson -> bolusRateJsonAdapter.fromJson(rateJson) }.toMutableList()
        return bp
    }

    @ToJson fun toJson(pattern: BolusPattern): BolusPatternJson {
        val rates = pattern.rates.map { rate ->
           return@map bolusRateJsonAdapter.toJson(rate)
        }
        return BolusPatternJson(
                primaryId = pattern.primaryId,
                rates = rates
        )
    }
}
