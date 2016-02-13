package com.nlefler.glucloser.dataSource.jsonAdapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.*

/**
 * Created by nathan on 2/8/16.
 */
class EJsonAdapter() {
    @FromJson fun fromJson(json: Map<String, String>): Date {
        val time = json["\$date"]?.toLong() ?: 0
        return Date(time)
    }

    @ToJson fun toJson(date: Date): Map<String, String> {
        return mapOf(Pair("\$date", date.time.toString()))
    }
}
