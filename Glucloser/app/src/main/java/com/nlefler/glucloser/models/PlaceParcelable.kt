package com.nlefler.glucloser.models

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public data class PlaceParcelable() : Parcelable {
    public var name: String? = null
    public var foursquareId: String? = null
    public var latitude: Float = 0.toFloat()
    public var longitude: Float = 0.toFloat()

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
        public val CREATOR: Parcelable.Creator<PlaceParcelable> = object : Parcelable.Creator<PlaceParcelable> {
            override fun createFromParcel(parcel: Parcel): PlaceParcelable {
                return PlaceParcelable(parcel)
            }

            override fun newArray(size: Int): Array<PlaceParcelable> {
                return Array(size, {i -> PlaceParcelable()})
            }
        }
    }
}
