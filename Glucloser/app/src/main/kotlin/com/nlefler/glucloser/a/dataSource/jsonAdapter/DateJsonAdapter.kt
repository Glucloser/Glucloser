package com.nlefler.glucloser.a.dataSource.jsonAdapter

import android.util.Log
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by nathan on 4/13/16.
 */
class DateJsonAdapter {
    // TODO(nl) Use device locale
    val formatter =  SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US)
    @FromJson fun fromJson(json: String): Date {
        return formatter.parse(json)
    }

    @ToJson fun toJson(date: Date): String {
        val s = formatter.format(date)
        return s
    }

}
