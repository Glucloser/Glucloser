package com.nlefler.glucloser.a.dataSource

import android.os.Bundle
import android.util.Log

import com.nlefler.glucloser.a.dataSource.jsonAdapter.PlaceJsonAdapter
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.models.CheckInPushedData
import com.nlefler.glucloser.a.models.Place
import com.nlefler.glucloser.a.models.PlaceEntity
import com.nlefler.glucloser.a.models.parcelable.PlaceParcelable
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.requery.kotlin.desc
import io.requery.kotlin.eq
import io.requery.query.Result
import rx.Observable

import java.util.*
import javax.inject.Inject

/**
 * Created by Nathan Lefler on 12/24/14.
 */
class PlaceFactory @Inject constructor(val dbManager: DBManager) {
    private val LOG_TAG = "PlaceFactory"

    fun placeForId(id: String): Observable<Result<PlaceEntity>> {
        if (id.isEmpty()) {
            return Observable.error(Exception("Invalid Id"))
        }
        return dbManager.data.select(PlaceEntity::class).where(Place::primaryId.eq(id)).get().toSelfObservable()
    }

    fun mostUsedPlaces(limit: Int = 10): Observable<Result<PlaceEntity>> {
        return dbManager.data.select(PlaceEntity::class).orderBy(Place::visitCount.desc()).limit(limit).get().toSelfObservable()
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

    fun jsonAdapter(): JsonAdapter<PlaceEntity> {
        return Moshi.Builder()
                .add(PlaceJsonAdapter())
                .build().adapter(PlaceEntity::class.java)
    }

    fun placeFromFoursquareVenue(venue: NLFoursquareVenue?): Observable<Place> {
        if (venue == null || !IsVenueValid(venue)) {
            val errorMessage = "Unable to create Place from 4sq venue"
            Log.e(LOG_TAG, errorMessage)
            return Observable.error(Exception(errorMessage))
        }

        return placeForFoursquareId(venue.id).map { result ->
            var place = result.firstOrNull()
            if (place != null) {
                return@map place
            }
            place = PlaceEntity()
            place.foursquareId = venue.id
            place.name = venue.name
            place.latitude = venue.location.lat
            place.longitude = venue.location.lng
            place.visitCount = 0

            return@map place
        }
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
        val place = PlaceEntity()
        place.primaryId = parcelable.primaryId
        place.foursquareId = parcelable.foursquareId
        place.name = parcelable.name
        place.latitude = parcelable.latitude
        place.longitude = parcelable.longitude
        place.visitCount = parcelable.visitCount
        return place
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

    private fun placeForFoursquareId(id: String): Observable<Result<PlaceEntity>> {
        if (id.isEmpty()) {
            return Observable.error(Exception("Invalid Id"))
        }
        return dbManager.data.select(PlaceEntity::class).where(Place::foursquareId.eq(id)).get().toSelfObservable()
    }

    private fun IsVenueValid(venue: NLFoursquareVenue?): Boolean {
        return venue != null && venue.id != null && !venue.id.isEmpty() && venue.name != null && !venue.name.isEmpty()
    }
}
