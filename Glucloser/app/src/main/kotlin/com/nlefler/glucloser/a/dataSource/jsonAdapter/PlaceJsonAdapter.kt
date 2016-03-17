package com.nlefler.glucloser.a.dataSource.jsonAdapter

import com.nlefler.glucloser.a.models.Place
import com.nlefler.glucloser.a.models.json.PlaceJson
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import io.realm.Realm

/**
 * Created by nathan on 1/31/16.
 */
public class PlaceJsonAdapter(val realm: Realm) {
    companion object {
        private val LOG_TAG = "PlaceJsonAdapter"
    }

    @FromJson fun fromJson(json: PlaceJson): Place {
        val place = realm.createObject(Place::class.java)

        realm.executeTransaction {
            place.primaryId = json.primaryId
            place.name = json.name
            place.foursquareId = json.foursquareId
            place.latitude = json.latitude
            place.longitude = json.longitude
        }

        return place
    }

    @ToJson fun toJson(place: Place): PlaceJson {
        val json = PlaceJson(primaryId = place.primaryId,
                foursquareId = place.foursquareId,
                name = place.name,
                latitude = place.latitude,
                longitude = place.longitude)
        return json
    }
}
