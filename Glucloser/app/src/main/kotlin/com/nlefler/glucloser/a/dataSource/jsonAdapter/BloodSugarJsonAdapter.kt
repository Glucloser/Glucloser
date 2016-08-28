package com.nlefler.glucloser.a.dataSource.jsonAdapter

import com.nlefler.glucloser.a.models.BloodSugar
import com.nlefler.glucloser.a.models.BloodSugarEntity
import com.nlefler.glucloser.a.models.json.BloodSugarJson
import com.nlefler.glucloser.a.models.parcelable.BloodSugarParcelable
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

/**
 * Created by nathan on 1/31/16.
 */
public class BloodSugarJsonAdapter() {
    @FromJson fun fromJson(json: BloodSugarJson): BloodSugar {
        val bs = BloodSugarParcelable()
        bs.primaryId = json.primaryId
        bs.readingValue = json.value
        bs.recordedDate = json.date
        return bs
    }

    @ToJson fun toJson(sugar: BloodSugar): BloodSugarJson {
        val json = BloodSugarJson(
                primaryId = sugar.primaryId,
                date = sugar.recordedDate,
                value = sugar.readingValue
        )
        return json
    }
}
