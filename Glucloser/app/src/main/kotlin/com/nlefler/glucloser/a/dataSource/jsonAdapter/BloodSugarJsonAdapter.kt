package com.nlefler.glucloser.a.dataSource.jsonAdapter

import com.nlefler.glucloser.a.models.BloodSugar
import com.nlefler.glucloser.a.models.json.BloodSugarJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

/**
 * Created by nathan on 1/31/16.
 */
public class BloodSugarJsonAdapter() {
    @FromJson fun fromJson(json: BloodSugarJson): BloodSugar {
        return BloodSugar(json.primaryId, json.value, json.date)
    }

    @ToJson fun toJson(sugar: BloodSugar): BloodSugarJson {
        val json = BloodSugarJson(
                primaryId = sugar.primaryId,
                date = sugar.recordedDate,
                value = sugar.value
        )
        return json
    }
}
