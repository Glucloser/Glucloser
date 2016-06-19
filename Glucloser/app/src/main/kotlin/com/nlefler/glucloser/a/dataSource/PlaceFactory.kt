package com.nlefler.glucloser.a.dataSource

import android.os.Bundle
import android.util.Log
import bolts.Continuation
import bolts.Task
import bolts.TaskCompletionSource

import com.nlefler.glucloser.a.dataSource.jsonAdapter.PlaceJsonAdapter
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.db.SQLStmts
import com.nlefler.glucloser.a.models.CheckInPushedData
import com.nlefler.glucloser.a.models.Place
import com.nlefler.glucloser.a.models.parcelable.PlaceParcelable
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

import java.util.*
import javax.inject.Inject

/**
 * Created by Nathan Lefler on 12/24/14.
 */
class PlaceFactory @Inject constructor(val dbManager: DBManager) {
    private val LOG_TAG = "PlaceFactory"

    fun placeForId(id: String): Task<Place> {
        val task = TaskCompletionSource<Place>()
        val query = SQLStmts.Place.ForID()
        dbManager.query(query, arrayOf(id), { cursor ->
            if (cursor == null) {
                task.setError(Exception("Unable to read db"))
                return@query
            }
            if (!cursor.moveToFirst()) {
                task.setError(Exception("No result for id"))
                return@query
            }
            task.setResult(Place(query.getID(cursor), query.getFoursquareID(cursor), query.getName(cursor),
                    query.getLatitude(cursor), query.getLongitude(cursor), query.getVisitCount(cursor)))
            cursor.close()
        })
        return task.task
    }

    fun mostUsedPlaces(limit: Int): Task<List<Place>> {
        val task = TaskCompletionSource<List<Place>>()
        val query = SQLStmts.Place.MostUsed(limit)
        dbManager.query(query, null, { cursor ->
            if (cursor == null) {
                task.setError(Exception("Unable to read db"))
                return@query
            }
            if (!cursor.moveToFirst()) {
                task.setError(Exception("No result for id"))
                return@query
            }
            val places = ArrayList<Place>()
            do {
                places.add(Place(query.getID(cursor), query.getFoursquareID(cursor), query.getName(cursor),
                        query.getLatitude(cursor), query.getLongitude(cursor), query.getVisitCount(cursor)))
            } while (cursor.moveToNext())
            task.setResult(places)
            cursor.close()
        })
        return task.task
    }

    fun parcelableFromFoursquareVenue(venue: NLFoursquareVenue?): PlaceParcelable? {
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

    fun jsonAdapter(): JsonAdapter<Place> {
        return Moshi.Builder()
                .add(PlaceJsonAdapter())
                .build().adapter(Place::class.java)
    }

    fun placeFromFoursquareVenue(venue: NLFoursquareVenue?): Task<Place> {
        if (venue == null || !IsVenueValid(venue)) {
            val errorMessage = "Unable to create Place from 4sq venue"
            Log.e(LOG_TAG, errorMessage)
            return Task.forError(Exception(errorMessage))
        }

        return placeForFoursquareId(venue.id)
                .continueWithTask(Continuation<Place, Task<Place>> placeForId@ { task ->
            if (task.isFaulted) {
                return@placeForId Task.forResult(Place(foursquareId = venue.id, name = venue.name, latitude = venue.location.lat,
                        longitude = venue.location.lng))
            }
            else {
                return@placeForId task
            }
        })
    }

    fun parcelableFromPlace(place: Place): PlaceParcelable? {
        val parcelable = PlaceParcelable()
        parcelable.name = place.name
        parcelable.foursquareId = place.foursquareId
        parcelable.latitude = place.latitude
        parcelable.longitude = place.longitude

        return parcelable
    }

    fun placeFromParcelable(parcelable: PlaceParcelable): Place {
        return Place(parcelable.primaryId, parcelable.foursquareId, parcelable.name,
                parcelable.latitude, parcelable.longitude)
    }

    fun arePlacesEqual(place1: Place?, place2: Place?): Boolean {
        if (place1 == null || place2 == null) {
            return false
        }

        val idOK = place1.foursquareId == place2.foursquareId
        val nameOK = place1.name == place2.name
        val latOK = place1.latitude == place2.latitude
        val lonOK = place1.longitude == place2.longitude

        return idOK && nameOK && latOK && lonOK
    }

    fun placeParcelableFromCheckInData(data: Bundle?): PlaceParcelable? {
        if (data == null) {
            Log.e(LOG_TAG, "Cannot create Place from check-in data, bundle null")
            return null
        }
        val checkInDataSerialized = data.getString("venueData")
        if (checkInDataSerialized == null || checkInDataSerialized.isEmpty()) {
            Log.e(LOG_TAG, "Cannot create Place from check-in data, parse bundle null")
            return null
        }
        val jsonAdapter = Moshi.Builder().build().adapter(CheckInPushedData::class.java)
        val checkInData = jsonAdapter.fromJson(checkInDataSerialized)
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

    private fun placeForFoursquareId(id: String): Task<Place> {
        val task = TaskCompletionSource<Place>()
        val query = SQLStmts.Place.ForFoursquareID()
        dbManager.query(query, arrayOf(id), { cursor ->
            if (cursor == null) {
                task.setError(Exception("Unable to read db"))
                return@query
            }
            if (!cursor.moveToFirst()) {
                task.setError(Exception("No result for id"))
                return@query
            }
            task.setResult(Place(query.getID(cursor), query.getFoursquareID(cursor), query.getName(cursor),
                    query.getLatitude(cursor), query.getLongitude(cursor), query.getVisitCount(cursor)))
            cursor.close()
        })
        return task.task
    }
    private fun IsVenueValid(venue: NLFoursquareVenue?): Boolean {
        return venue != null && venue.id != null && !venue.id.isEmpty() && venue.name != null && !venue.name.isEmpty()
    }
}
