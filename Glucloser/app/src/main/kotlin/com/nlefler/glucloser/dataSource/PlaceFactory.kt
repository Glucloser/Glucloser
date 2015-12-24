package com.nlefler.glucloser.dataSource

import android.content.Context
import android.os.Bundle
import android.util.Log
import bolts.Continuation
import bolts.Task
import bolts.TaskCompletionSource

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
import javax.inject.Inject

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class PlaceFactory @Inject constructor(val realmManager: RealmManager) {
    private val LOG_TAG = "PlaceFactory"

    public fun placeForId(id: String): Task<Place?> {
        return placeForFoursquareId(id, false).continueWithTask(Continuation<Place?, Task<Place?>> { task ->
            if (task.isFaulted) {
                return@Continuation task
            }
            val place = task.result
            if (place != null) {
                return@Continuation Task.forResult(place)
            }
            val parseTask = TaskCompletionSource<Place?>()
            val parseQuery = ParseQuery.getQuery<ParseObject>(Place.ParseClassName)
            parseQuery.whereEqualTo(Place.FoursquareIdFieldName, id)
            parseQuery.findInBackground({parseObjects: List<ParseObject>, e: ParseException? ->
                if (parseObjects.isEmpty()) {
                    parseTask.trySetError(Exception("No Place ParseObjects for id ${id}"))
                    return@findInBackground
                }
                placeFromParseObject(parseObjects.get(0)).continueWith { task ->
                    if (task.isFaulted) {
                        parseTask.trySetError(task.error)
                        return@continueWith
                    }
                    parseTask.trySetResult(task.result)
                }
            })
            return@Continuation parseTask.task
        })
    }

    public fun parcelableFromFoursquareVenue(venue: NLFoursquareVenue?): PlaceParcelable? {
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

    public fun placeFromFoursquareVenue(venue: NLFoursquareVenue?): Task<Place?> {
        if (venue == null || !IsVenueValid(venue)) {
            val errorMessage = "Unable to create Place from 4sq venue"
            Log.e(LOG_TAG, errorMessage)
            return Task.forError(Exception(errorMessage))
        }

        return placeForFoursquareId(venue.id, true).continueWithTask(Continuation<Place?, Task<Place?>> { task ->
            if (task.isFaulted) {
                return@Continuation task
            }
            val place = task.result
            val realmTask = TaskCompletionSource<Place?>()
            realmManager.executeTransaction(Realm.Transaction { realm ->
                place?.name = venue.name
                place?.foursquareId = venue.id
                place?.latitude = venue.location.lat
                place?.longitude = venue.location.lng
                realmTask.trySetResult(place)
            }, realmTask.task)

        })
    }

    public fun parcelableFromPlace(place: Place): PlaceParcelable? {
        val parcelable = PlaceParcelable()
        parcelable.name = place.name
        parcelable.foursquareId = place.foursquareId
        parcelable.latitude = place.latitude
        parcelable.longitude = place.longitude

        return parcelable
    }

    public fun placeFromParcelable(parcelable: PlaceParcelable): Task<Place?> {
        return placeForFoursquareId(parcelable.foursquareId, true).continueWithTask(Continuation<Place?, Task<Place?>> { task ->
            if (task.isFaulted) {
                return@Continuation task
            }

            val place = task.result
            val realmTask = TaskCompletionSource<Place?>()
            realmManager.executeTransaction(Realm.Transaction { realm ->
                place?.name = parcelable.name
                place?.foursquareId = parcelable.foursquareId
                place?.latitude = parcelable.latitude
                place?.longitude = parcelable.longitude
                realmTask.trySetResult(place)
            }, realmTask.task)
        })
    }

    public fun arePlacesEqual(place1: Place?, place2: Place?): Boolean {
        if (place1 == null || place2 == null) {
            return false
        }

        val idOK = place1.foursquareId == place2.foursquareId
        val nameOK = place1.name == place2.name
        val latOK = place1.latitude == place2.latitude
        val lonOK = place1.longitude == place2.longitude

        return idOK && nameOK && latOK && lonOK
    }

    internal fun placeFromParseObject(parseObject: ParseObject): Task<Place?> {
        val foursquareId = parseObject.getString(Place.FoursquareIdFieldName)
        if (foursquareId == null || foursquareId.isEmpty()) {
            return Task.forError(Exception("Invalid Place ParseObject, no Foursquare Id"))
        }

        val name = parseObject.getString(Place.NameFieldName)
        val lat = parseObject.getDouble(Place.LatitudeFieldName).toFloat()
        val lon = parseObject.getDouble(Place.LongitudeFieldName).toFloat()

        return placeForFoursquareId(foursquareId, true).continueWithTask(Continuation<Place?, Task<Place?>> { task ->
            if (task.isFaulted) {
                return@Continuation task
            }
            val place = task.result
            val realmTask = TaskCompletionSource<Place?>()
            realmManager.executeTransaction(Realm.Transaction { realm ->
                if (place?.foursquareId?.isEmpty() ?: false) {
                    place?.foursquareId = foursquareId
                }
                place?.name = name
                if (lat != 0f && place?.latitude != lat) {
                    place?.latitude = lat
                }
                if (lon != 0f && place?.longitude != lon) {
                    place?.longitude = lon
                }
                realmTask.trySetResult(place)
            }, realmTask.task)
        })
    }

    /**
     * Fetches or creates a ParseObject representing the provided Place.
     * @param place
     * *
     * @param action Returns the fetched/created ParseObject, and true if the object was created
     * *               and should be saved.
     */
    internal fun parseObjectFromPlace(place: Place?, action: Action2<ParseObject?, Boolean>?) {
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

    public fun placeParcelableFromCheckInData(data: Bundle?): PlaceParcelable? {
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

    private fun placeForFoursquareId(id: String?, create: Boolean): Task<Place?> {
        val realmTask = TaskCompletionSource<Place?>()
        return realmManager.executeTransaction(Realm.Transaction { realm ->
            if (create && (id == null || id.isEmpty())) {
                val place = realm.createObject<Place>(Place::class.java)
                realmTask.trySetResult(place)
                return@Transaction
            }

            val query = realm.where<Place>(Place::class.java)

            query?.equalTo(Place.FoursquareIdFieldName, id)
            var result: Place? = query?.findFirst()

            if (result == null && create) {
                result = realm.createObject<Place>(Place::class.java)
                result!!.foursquareId = id
                realmTask.trySetResult(result)
            }
            else if (result == null) {
                realmTask.trySetError(Exception("No Place for id ${id} and create is false"))
            }
            else {
                realmTask.trySetResult(result)
            }
        }, realmTask.task)
    }

    private fun IsVenueValid(venue: NLFoursquareVenue?): Boolean {
        return venue != null && venue.id != null && !venue.id.isEmpty() && venue.name != null && !venue.name.isEmpty()
    }
}
