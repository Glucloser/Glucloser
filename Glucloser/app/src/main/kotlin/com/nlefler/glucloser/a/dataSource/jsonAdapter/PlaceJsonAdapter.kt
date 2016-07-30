package com.nlefler.glucloser.a.dataSource.jsonAdapter

import com.nlefler.glucloser.a.models.Place
import com.nlefler.glucloser.a.models.json.PlaceJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

/**
 * Created by nathan on 1/31/16.
 */
public class PlaceJsonAdapter() {
    companion object {
        private val LOG_TAG = "PlaceJsonAdapter"
    }

    @FromJson fun fromJson(json: PlaceJson): Place {
        return Place(json.primaryId, json.name, json.foursquareId, json.latitude, json.longitude, json.visitCount)
    }

    @ToJson fun toJson(place: Place): PlaceJson {
        return PlaceJson(primaryId = place.primaryId,
                foursquareId = place.foursquareId,
                name = place.name,
                latitude = place.latitude,
                longitude = place.longitude,
                visitCount = place.visitCount)
    }
}
