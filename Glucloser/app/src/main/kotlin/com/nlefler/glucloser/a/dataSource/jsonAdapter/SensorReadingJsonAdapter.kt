package com.nlefler.glucloser.a.dataSource.jsonAdapter

import com.nlefler.glucloser.a.models.SensorReading
import com.nlefler.glucloser.a.models.SensorReadingEntity
import com.nlefler.glucloser.a.models.json.SensorReadingJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

/**
 * Created by nathan on 3/26/16.
 */
class SensorReadingJsonAdapter {
   @FromJson fun fromJson(json: SensorReadingJson): SensorReading {
        val bs = SensorReadingEntity()
        bs.primaryId = json.primaryId
        bs.readingValue = json.readingValue
        bs.recordedDate = json.recordedDate
        return bs
    }

    @ToJson fun toJson(reading: SensorReading): SensorReadingJson {
        val json = SensorReadingJson(
                primaryId = reading.primaryId,
                recordedDate = reading.recordedDate,
                readingValue = reading.readingValue
        )
        return json
    }
}
