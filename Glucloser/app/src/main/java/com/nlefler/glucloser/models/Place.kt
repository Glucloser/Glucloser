package com.nlefler.glucloser.models

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.RealmClass

/**
 * Created by Nathan Lefler on 12/11/14.
 */
@RealmClass
public open data class Place : RealmObject() {
    public open var name: String? = null
    public open var foursquareId: String? = null
    public open var latitude: Float = 0.toFloat()
    public open var longitude: Float = 0.toFloat()

    companion object {
        @Ignore
        public val ParseClassName: String = "Place"

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
