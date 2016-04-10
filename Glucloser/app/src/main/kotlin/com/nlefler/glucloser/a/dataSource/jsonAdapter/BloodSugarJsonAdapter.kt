package com.nlefler.glucloser.a.dataSource.jsonAdapter

import com.nlefler.glucloser.a.models.BloodSugar
import com.nlefler.glucloser.a.models.json.BloodSugarJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.realm.Realm

/**
 * Created by nathan on 1/31/16.
 */
public class BloodSugarJsonAdapter(val realm: Realm) {
    @FromJson fun fromJson(json: BloodSugarJson): BloodSugar {
        val sugar = realm.createObject(BloodSugar::class.java)

        realm.executeTransaction {
            sugar.primaryId = json.primaryId
            sugar.recordedDate = json.date
            sugar.value = json.value
        }

        return sugar
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
