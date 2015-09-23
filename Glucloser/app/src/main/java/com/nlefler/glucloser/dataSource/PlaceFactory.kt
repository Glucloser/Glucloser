package com.nlefler.glucloser.dataSource

import android.content.Context
import android.os.Bundle
import android.util.Log

import com.google.gson.Gson
import com.nlefler.glucloser.models.CheckInPushedData
import com.nlefler.glucloser.models.Place
import com.nlefler.glucloser.models.PlaceParcelable
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue
import com.parse.FindCallback
import com.parse.GetCallback
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery

import io.realm.Realm
import io.realm.RealmQuery
import io.realm.RealmResults
import rx.functions.Action1
import rx.functions.Action2

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class PlaceFactory {
    companion object {
        private val LOG_TAG = "PlaceFactory"

        public fun PlaceForId(id: String?, ctx: Context?, action: Action1<Place>?) {
            if (action == null) {
                Log.e(LOG_TAG, "Unable to fetch Place for id, action is null")
                return
            }
            if (id == null || id.isEmpty() || ctx == null) {
                Log.e(LOG_TAG, "Unable to fetch Place for id")
                action.call(null)
                return
            }

            val realm = Realm.getInstance(ctx)
            val place = PlaceForFoursquareId(id, realm, false)
            if (place != null) {
                action.call(place)
                return
            }

            val parseQuery = ParseQuery.getQuery<ParseObject>(Place.ParseClassName)
            parseQuery.whereEqualTo(Place.FoursquareIdFieldName, id)
            parseQuery.findInBackground({parseObjects: List<ParseObject>, e: ParseException? ->
                if (!parseObjects.isEmpty()) {
                    val placeFromParse = PlaceFromParseObject(parseObjects.get(0), realm)
                    action.call(placeFromParse)
                } else {
                    action.call(null)
                }
            })
        }

        public fun ParcelableFromFoursquareVenue(venue: NLFoursquareVenue?): PlaceParcelable? {
            if (venue == null || !IsVenueValid(venue)) {
                Log.e(LOG_TAG, "Unable to create Place from 4sq venue")
                return null
            }

            val parcelable = PlaceParcelable()
            parcelable.name = venue.name
            parcelable.foursquareId = venue.id
            parcelable.latitude = venue.location.lat
            parcelable.longitude = venue.location.lng

            return parcelable
        }

        public fun PlaceFromFoursquareVenue(venue: NLFoursquareVenue?, ctx: Context?): Place? {
            if (venue == null || !IsVenueValid(venue) || ctx == null) {
                Log.e(LOG_TAG, "Unable to create Place from 4sq venue")
                return null
            }

            val realm = Realm.getInstance(ctx)

            realm.beginTransaction()
            val place = PlaceForFoursquareId(venue.id, realm, true)
            place!!.name = venue.name
            place.foursquareId = venue.id
            place.latitude = venue.location.lat
            place.longitude = venue.location.lng
            realm.commitTransaction()

            return place
        }

        public fun ParcelableFromPlace(place: Place): PlaceParcelable? {
            val parcelable = PlaceParcelable()
            parcelable.name = place.name
            parcelable.foursquareId = place.foursquareId
            parcelable.latitude = place.latitude
            parcelable.longitude = place.longitude

            return parcelable
        }

        public fun PlaceFromParcelable(parcelable: PlaceParcelable, ctx: Context): Place {
            val realm = Realm.getInstance(ctx)

            realm.beginTransaction()
            val place = PlaceForFoursquareId(parcelable.foursquareId, realm, true)
            place!!.name = parcelable.name
            place.foursquareId = parcelable.foursquareId
            place.latitude = parcelable.latitude
            place.longitude = parcelable.longitude
            realm.commitTransaction()

            return place
        }

        public fun ArePlacesEqual(place1: Place?, place2: Place?): Boolean {
            if (place1 == null || place2 == null) {
                return false
            }

            val idOK = place1.foursquareId == place2.foursquareId
            val nameOK = place1.name == place2.name
            val latOK = place1.latitude == place2.latitude
            val lonOK = place1.longitude == place2.longitude

            return idOK && nameOK && latOK && lonOK
        }

        internal fun PlaceFromParseObject(parseObject: ParseObject?, realm: Realm?): Place? {
            if (parseObject == null || realm == null) {
                return null
            }
            val foursquareId = parseObject.getString(Place.FoursquareIdFieldName)
            if (foursquareId == null || foursquareId.isEmpty()) {
                return null
            }

            val name = parseObject.getString(Place.NameFieldName)
            val lat = parseObject.getDouble(Place.LatitudeFieldName).toFloat()
            val lon = parseObject.getDouble(Place.LongitudeFieldName).toFloat()

            realm.beginTransaction()
            val place = PlaceForFoursquareId(foursquareId, realm, true)!!
            if (place.foursquareId?.isEmpty() ?: false) {
                place.foursquareId = foursquareId
            }
            place.name = name
            if (lat != 0f && place.latitude != lat) {
                place.latitude = lat
            }
            if (lon != 0f && place.longitude != lon) {
                place.longitude = lon
            }
            realm.commitTransaction()

            return place
        }

        /**
         * Fetches or creates a ParseObject representing the provided Place.
         * @param place
         * *
         * @param action Returns the fetched/created ParseObject, and true if the object was created
         * *               and should be saved.
         */
        internal fun ParseObjectFromPlace(place: Place?, action: Action2<ParseObject?, Boolean>?) {
            if (action == null) {
                Log.e(LOG_TAG, "Unable to create Parse object from Place, action null")
                return
            }
            if (place == null || place.foursquareId == null || place.foursquareId!!.isEmpty()) {
                Log.e(LOG_TAG, "Unable to create Parse object from Place, place null or no Foursquare id")
                action.call(null, false)
                return
            }

            val parseQuery = ParseQuery.getQuery<ParseObject>(Place.ParseClassName)
            parseQuery.whereEqualTo(Place.FoursquareIdFieldName, place.foursquareId)

            parseQuery.findInBackground({parseObjects: List<ParseObject>, e: ParseException? ->
                val parseObject: ParseObject
                var created = false
                if (parseObjects.isEmpty()) {
                    parseObject = ParseObject(Place.ParseClassName)
                    created = true
                } else {
                    parseObject = parseObjects.get(0)
                }
                parseObject.put(Place.FoursquareIdFieldName, place.foursquareId)
                parseObject.put(Place.NameFieldName, place.name)
                parseObject.put(Place.LatitudeFieldName, place.latitude)
                parseObject.put(Place.LongitudeFieldName, place.longitude)
                action.call(parseObject, created)
            })
        }

        public fun PlaceParcelableFromCheckInData(data: Bundle?): PlaceParcelable? {
            if (data == null) {
                Log.e(LOG_TAG, "Cannot create Place from check-in data, bundle null")
                return null
            }
            val checkInDataSerialized = data.getString("com.parse.Data")
            if (checkInDataSerialized == null || checkInDataSerialized.isEmpty()) {
                Log.e(LOG_TAG, "Cannot create Place from check-in data, parse bundle null")
                return null
            }
            val checkInData = (Gson()).fromJson<CheckInPushedData>(checkInDataSerialized, CheckInPushedData::class.java)
            if (checkInData == null) {
                Log.e(LOG_TAG, "Cannot create Place from check-in data, couldn't parse data")
                return null
            }

            val placeParcelable = PlaceParcelable()
            placeParcelable.foursquareId = checkInData.venueId
            placeParcelable.name = checkInData.venueName
            if (checkInData.venueLat != 0f) {
                placeParcelable.latitude = checkInData.venueLat
            }
            if (checkInData.venueLon != 0f) {
                placeParcelable.longitude = checkInData.venueLon
            }

            return placeParcelable
        }

        private fun PlaceForFoursquareId(id: String?, realm: Realm, create: Boolean): Place? {
            if (create && (id == null || id.isEmpty())) {
                return realm.createObject<Place>(Place::class.java)
            }

            val query = realm.where<Place>(Place::class.java)

            query.equalTo(Place.FoursquareIdFieldName, id)
            var result: Place? = query.findFirst()

            if (result == null && create) {
                result = realm.createObject<Place>(Place::class.java)
                result!!.foursquareId = id
            }

            return result
        }

        private fun IsVenueValid(venue: NLFoursquareVenue?): Boolean {
            return venue != null && venue.id != null && !venue.id.isEmpty() && venue.name != null && !venue.name.isEmpty()
        }
    }
}
