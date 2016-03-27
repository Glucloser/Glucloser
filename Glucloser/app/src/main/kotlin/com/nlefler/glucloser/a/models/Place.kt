package com.nlefler.glucloser.a.models

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by Nathan Lefler on 12/11/14.
 */
@RealmClass
open class Place(
        @PrimaryKey open var primaryId: String = UUID.randomUUID().toString(),
        open var name: String = "",
        open var foursquareId: String = UUID.randomUUID().toString(),
        open var latitude: Float = 0f,
        open var longitude: Float = 0f,
        open var visitCount: Int = 0
    ) : RealmObject(), Syncable {

    companion object {
        @Ignore
        val ModelName: String = "places"

        @Ignore
        val PrimaryKeyName: String = "primaryId"

        @Ignore
        val NameFieldName: String = "name"

        @Ignore
        val FoursquareIdFieldName: String = "foursquareId"

        @Ignore
        val LatitudeFieldName: String = "latitude"

        @Ignore
        val LongitudeFieldName: String = "longitude"

        @Ignore
        val VisitCountFieldName: String = "visitCount"
    }
}
