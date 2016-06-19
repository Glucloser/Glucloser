package com.nlefler.glucloser.a.models.parcelable

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class PlaceParcelable() : Parcelable {
    var primaryId: String
    public var name: String
    var foursquareId: String
    var latitude: Float
    var longitude: Float

    /** Parcelable  */
    protected constructor(parcel: Parcel): this() {
        name = parcel.readString()
        foursquareId = parcel.readString()
        latitude = parcel.readFloat()
        longitude = parcel.readFloat()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(foursquareId)
        dest.writeFloat(latitude)
        dest.writeFloat(longitude)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<PlaceParcelable> = object : Parcelable.Creator<PlaceParcelable> {
            override fun createFromParcel(parcel: Parcel): PlaceParcelable {
                return PlaceParcelable(parcel)
            }

            override fun newArray(size: Int): Array<PlaceParcelable> {
                return Array(size, {i -> PlaceParcelable() })
            }
        }
    }
}
