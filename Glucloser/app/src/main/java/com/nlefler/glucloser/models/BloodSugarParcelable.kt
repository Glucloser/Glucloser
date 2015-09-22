package com.nlefler.glucloser.models

import android.os.Parcel
import android.os.Parcelable

import java.util.Date

/**
 * Created by Nathan Lefler on 1/4/15.
 */
public data class BloodSugarParcelable() : Parcelable {
    public var id: String? = null
    public var value: Int = 0
    public var date: Date? = null

    /** Parcelable  */
    protected constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        value = parcel.readInt()
        val time = parcel.readLong()
        if (time > 0) {
            date = Date(time)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeInt(value)
        if (date != null) {
            dest.writeLong(date!!.time)
        }
    }

    companion object {
        public val CREATOR: Parcelable.Creator<BloodSugarParcelable> = object : Parcelable.Creator<BloodSugarParcelable> {
            override fun createFromParcel(`in`: Parcel): BloodSugarParcelable {
                return BloodSugarParcelable(`in`)
            }

            override fun newArray(size: Int): Array<BloodSugarParcelable> {
                return Array(size, {i -> BloodSugarParcelable()})
            }
        }
    }
}
