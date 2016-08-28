package com.nlefler.glucloser.a.dataSource.jsonAdapter

import com.nlefler.glucloser.a.models.BolusRate
import com.nlefler.glucloser.a.models.BolusRateEntity
import com.nlefler.glucloser.a.models.json.BolusRateJson
import com.nlefler.glucloser.a.models.parcelable.BolusRateParcelable
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

/**
 * Created by nathan on 1/31/16.
 */
public class BolusRateJsonAdapter() {
    @FromJson fun fromJson(json: BolusRateJson): BolusRate {
        val br = BolusRateParcelable()
        br.primaryId = json.primaryId
        br.ordinal = json.ordinal
        br.startTime = json.startTime
        br.carbsPerUnit = json.carbsPerUnit
        return br
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
