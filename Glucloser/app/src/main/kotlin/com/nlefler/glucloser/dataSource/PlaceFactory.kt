package com.nlefler.glucloser.dataSource

import android.os.Bundle
import android.util.Log
import bolts.Continuation
import bolts.Task

import com.google.gson.Gson
import com.nlefler.glucloser.dataSource.jsonAdapter.PlaceJsonAdapter
import com.nlefler.glucloser.models.CheckInPushedData
import com.nlefler.glucloser.models.Place
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

import io.realm.Realm
import io.realm.RealmObject
import javax.inject.Inject

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class PlaceFactory @Inject constructor(val realmManager: RealmManager) {
    private val LOG_TAG = "PlaceFactory"

    public fun placeForId(id: String, create: Boolean): Task<Place?> {
        return realmManager.executeTransaction(object: RealmManager.Tx<Place?> {
            override fun dependsOn(): List<RealmObject?> {
                return emptyList()
            }

            override fun execute(dependsOn: List<RealmObject?>, realm: Realm): Place? {
                if (create && id.isEmpty()) {
                    val place = realm.createObject<Place>(Place::class.java)
                    return place
                }

                val query = realm.where<Place>(Place::class.java)

                query?.equalTo(Place.FoursquareIdFieldName, id)
                var result: Place? = query?.findFirst()

                if (result == null && create) {
                    result = realm.createObject<Place>(Place::class.java)
                }
                return result
            }

        })
    }

    public fun jsonAdapter(): JsonAdapter<Place> {
        return Moshi.Builder().add(PlaceJsonAdapter(realmManager.defaultRealm())).build().adapter(Place::class.java)
    }

    public fun placeFromFoursquareVenue(venue: NLFoursquareVenue?): Task<Place?> {
        if (venue == null || !IsVenueValid(venue)) {
            val errorMessage = "Unable to create Place from 4sq venue"
            Log.e(LOG_TAG, errorMessage)
            return Task.forError(Exception(errorMessage))
        }

        return placeForFoursquareId(venue.id, true).continueWithTask(Continuation<Place?, Task<Place?>> placeForId@ { task ->
            if (task.isFaulted) {
                return@placeForId task
            }
            val place = task.result
            return@placeForId realmManager.executeTransaction(object: RealmManager.Tx<Place?> {
                override fun dependsOn(): List<RealmObject?> {
                    return listOf(place)
                }

                override fun execute(dependsOn: List<RealmObject?>, realm: Realm): Place? {
                    val livePlace = dependsOn.first() as Place?
                    livePlace?.name = venue.name
                    livePlace?.foursquareId = venue.id
                    livePlace?.latitude = venue.location.lat
                    livePlace?.longitude = venue.location.lng
                    return livePlace
                }
            })

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

    public fun placeFromCheckInData(data: Bundle?): Task<Place?> {
        if (data == null) {
            val msg = "Cannot create Place from check-in data, bundle null"
            Log.e(LOG_TAG, msg)
            return Task.forError(Exception(msg))
        }
        val checkInDataSerialized = data.getString("com.parse.Data")
        if (checkInDataSerialized == null || checkInDataSerialized.isEmpty()) {
            val msg = "Cannot create Place from check-in data, parse bundle null"
            Log.e(LOG_TAG, msg)
            return Task.forError(Exception(msg))
        }
        val checkInData = (Gson()).fromJson<CheckInPushedData>(checkInDataSerialized, CheckInPushedData::class.java)
        if (checkInData == null) {
            val msg = "Cannot create Place from check-in data, couldn't parse data"
            Log.e(LOG_TAG, msg)
            return Task.forError(Exception(msg))
        }

        return placeForFoursquareId(checkInData.venueId, true).continueWithTask { task ->
            if (task.isFaulted) {
                return@continueWithTask Task.forError<Place?>(task.error)
            }

            val place = task.result
            place?.foursquareId = checkInData.venueId
            place?.name = checkInData.venueName
            if (checkInData.venueLat != 0f) {
                place?.latitude = checkInData.venueLat
            }
            if (checkInData.venueLon != 0f) {
                place?.longitude = checkInData.venueLon
            }
            return@continueWithTask  Task.forResult(place)
        }
    }

    public fun placeForFoursquareId(id: String, create: Boolean): Task<Place?> {
        return realmManager.executeTransaction(object: RealmManager.Tx<Place?> {
            override fun dependsOn(): List<RealmObject?> {
                return emptyList()
            }

            override fun execute(dependsOn: List<RealmObject?>, realm: Realm): Place? {
                if (create && id.isEmpty()) {
                    val place = realm.createObject<Place>(Place::class.java)
                    return place
                }

                val query = realm.where<Place>(Place::class.java)

                query?.equalTo(Place.FoursquareIdFieldName, id)
                var result: Place? = query?.findFirst()

                if (result == null && create) {
                    result = realm.createObject<Place>(Place::class.java)
                    result!!.foursquareId = id
                }
                return result
            }

        })
    }

    private fun IsVenueValid(venue: NLFoursquareVenue?): Boolean {
        return venue != null && venue.id != null && !venue.id.isEmpty() && venue.name != null && !venue.name.isEmpty()
    }
}
