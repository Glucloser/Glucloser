package com.nlefler.glucloser.dataSource

import android.os.Bundle
import android.util.Log
import bolts.Continuation
import bolts.Task

import com.google.gson.Gson
import com.nlefler.glucloser.dataSource.jsonAdapter.EJsonAdapter
import com.nlefler.glucloser.dataSource.jsonAdapter.PlaceJsonAdapter
import com.nlefler.glucloser.models.CheckInPushedData
import com.nlefler.glucloser.models.Place
import com.nlefler.glucloser.models.parcelable.PlaceParcelable
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

import io.realm.Realm
import io.realm.RealmObject
import java.util.*
import javax.inject.Inject

/**
 * Created by Nathan Lefler on 12/24/14.
 */
class PlaceFactory @Inject constructor(val realmManager: RealmManager) {
    private val LOG_TAG = "PlaceFactory"

    fun placeForId(id: String): Task<Place?> {
        return placeForFoursquareId(id, false)
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
                .add(PlaceJsonAdapter(realmManager.defaultRealm()))
                .add(EJsonAdapter())
                .build().adapter(Place::class.java)
    }

    fun placeFromFoursquareVenue(venue: NLFoursquareVenue?): Task<Place?> {
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

    fun parcelableFromPlace(place: Place): PlaceParcelable? {
        val parcelable = PlaceParcelable()
        parcelable.name = place.name
        parcelable.foursquareId = place.foursquareId
        parcelable.latitude = place.latitude
        parcelable.longitude = place.longitude

        return parcelable
    }

    fun placeFromParcelable(parcelable: PlaceParcelable): Task<Place?> {
        return placeForFoursquareId(parcelable.foursquareId, true)
                .continueWithTask(Continuation<Place?, Task<Place?>> placeForId@ { task ->
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
                    livePlace?.name = parcelable.name
                    livePlace?.foursquareId = parcelable.foursquareId
                    livePlace?.latitude = parcelable.latitude
                    livePlace?.longitude = parcelable.longitude
                    return livePlace
                }
            })
        })
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

    private fun placeForFoursquareId(id: String, create: Boolean): Task<Place?> {
        val setDefaultValues = fun(place: Place) {
            place.primaryId = UUID.randomUUID().toString()
            place.foursquareId = UUID.randomUUID().toString()
            place.latitude = 0f
            place.longitude = 0f
            place.name = ""
        }

        return realmManager.executeTransaction(object: RealmManager.Tx<Place?> {
            override fun dependsOn(): List<RealmObject?> {
                return emptyList()
            }

            override fun execute(dependsOn: List<RealmObject?>, realm: Realm): Place? {
                if (create && id.isEmpty()) {
                    val place = realm.createObject<Place>(Place::class.java)
                    setDefaultValues(place)
                    return place
                }

                val query = realm.where<Place>(Place::class.java)

                query?.equalTo(Place.FoursquareIdFieldName, id)
                val foundPlace: Place? = query?.findFirst()
                if (foundPlace != null) {
                    return foundPlace
                }

                if (create) {
                    val place= realm.createObject<Place>(Place::class.java)
                    setDefaultValues(place)
                    return place
                }
                return null
            }

        })
    }

    private fun IsVenueValid(venue: NLFoursquareVenue?): Boolean {
        return venue != null && venue.id != null && !venue.id.isEmpty() && venue.name != null && !venue.name.isEmpty()
    }
}
