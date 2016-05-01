package com.nlefler.glucloser.a.dataSource.jsonAdapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.net.URL

/**
 * Created by nathan on 4/27/16.
 */
class UrlJsonAdapter {
    @FromJson
    fun fromJson(json: String): URL {
        return URL(json)
    }

    @ToJson
    fun toJson(url: URL): String {
        return url.toExternalForm()
    }
}