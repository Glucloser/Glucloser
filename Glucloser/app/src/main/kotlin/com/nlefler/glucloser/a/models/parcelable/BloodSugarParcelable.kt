package com.nlefler.glucloser.a.models.parcelable

import android.os.Parcel
import android.os.Parcelable
import com.nlefler.glucloser.a.models.BloodSugar
import java.util.*

/**
 * Created by Nathan Lefler on 1/4/15.
 */
class BloodSugarParcelable() : BloodSugar, Parcelable {
    override var primaryId: String = UUID.randomUUID().toString()
    override var readingValue: Int = 0
    override var recordedDate: Date = Date()

    /** Parcelable  */
    protected constructor(parcel: Parcel) : this() {
        primaryId = parcel.readString()
        readingValue = parcel.readInt()
        val time = parcel.readLong()
        if (time > 0) {
            recordedDate = Date(time)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(primaryId)
        dest.writeInt(readingValue)
        dest.writeLong(recordedDate.time)
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
