package com.nlefler.glucloser.a.dataSource.jsonAdapter

import android.util.Log
import com.nlefler.glucloser.a.models.BolusRate
import com.nlefler.glucloser.a.models.json.BolusRateJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.realm.Realm
import java.util.*

/**
 * Created by nathan on 1/31/16.
 */
public class BolusRateJsonAdapter(val realm: Realm) {
    @FromJson fun fromJson(json: BolusRateJson): BolusRate {
        var rate: BolusRate? = null
        realm.beginTransaction()
        try {
            rate = realm.createObject(BolusRate::class.java)
            rate?.primaryId = UUID.randomUUID().toString()
            rate?.ordinal = json.ordinal
            rate?.startTime = json.startTime
            rate?.carbsPerUnit = json.carbsPerUnit
        }
        catch (e:Exception) {
            Log.d("","")
        }
        realm.commitTransaction()

        return rate!!
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
