package com.nlefler.glucloser.a.models.parcelable

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Created by Nathan Lefler on 1/4/15.
 */
public class BloodSugarParcelable() : Parcelable {
    var id: String = UUID.randomUUID().toString()
    var value: Int = 0
    var date: Date = Date()

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
        dest.writeLong(date.time)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<BloodSugarParcelable> = object : Parcelable.Creator<BloodSugarParcelable> {
            override fun createFromParcel(`in`: Parcel): BloodSugarParcelable {
                return BloodSugarParcelable(`in`)
            }

            override fun newArray(size: Int): Array<BloodSugarParcelable> {
                return Array(size, {i -> BloodSugarParcelable() })
            }
        }
    }
}
