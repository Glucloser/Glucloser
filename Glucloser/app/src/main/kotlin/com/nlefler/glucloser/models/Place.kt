package com.nlefler.glucloser.models

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by Nathan Lefler on 12/11/14.
 */
@RealmClass
public open class Place(
        @PrimaryKey public open var primaryId: String = UUID.randomUUID().toString(),
        public open var name: String = "",
        public open var foursquareId: String = UUID.randomUUID().toString(),
        public open var latitude: Float = 0f,
        public open var longitude: Float = 0f
    ) : RealmObject() {

    companion object {
        @Ignore
        public val ModelName: String = "Place"

        @Ignore
        public val IdFieldName: String = "primaryId"

        @Ignore
        public val NameFieldName: String = "name"

        @Ignore
        public val FoursquareIdFieldName: String = "foursquareId"

        @Ignore
        public val LatitudeFieldName: String = "latitude"

        @Ignore
        public val LongitudeFieldName: String = "longitude"
    }
}
