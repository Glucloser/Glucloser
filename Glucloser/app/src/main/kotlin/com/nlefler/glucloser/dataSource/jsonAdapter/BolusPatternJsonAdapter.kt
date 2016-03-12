package com.nlefler.glucloser.dataSource.jsonAdapter

import android.util.Log
import com.nlefler.glucloser.dataSource.BolusRateFactory
import com.nlefler.glucloser.models.BolusPattern
import com.nlefler.glucloser.models.BolusRate
import com.nlefler.glucloser.models.json.BolusPatternJson
import com.nlefler.glucloser.models.json.BolusRateJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.realm.Realm
import io.realm.RealmList
import java.util.*

/**
 * Created by nathan on 1/31/16.
 */
public class BolusPatternJsonAdapter(val realm: Realm) {
    val rateAdapter = BolusRateJsonAdapter(realm)

    @FromJson fun fromJson(json: BolusPatternJson): BolusPattern {
        realm.beginTransaction()
        val pattern: BolusPattern = realm.createObject(BolusPattern::class.java)
        pattern.primaryId = UUID.randomUUID().toString()
        pattern.rates = RealmList()
        realm.commitTransaction()

        try {
            json.rates.forEach { rateJson ->
                val rate = rateAdapter.fromJson(rateJson) ?: return@forEach
                realm.beginTransaction()
                pattern.rates.add(rate)
                realm.commitTransaction()
            }
        } catch (e: Exception) {
            Log.d("","")
        }

        return realm.copyFromRealm(pattern)
    }

    @ToJson fun toJson(pattern: BolusPattern): BolusPatternJson {
        val rates = pattern.rates.map { rate ->
           return@map rateAdapter.toJson(rate)
        }
        val json = BolusPatternJson(
                rates = rates
        )
        return json
    }
}
