package com.nlefler.glucloser.models.parcelable

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Created by nathan on 9/20/15.
 */
public class BolusRateParcelable(): Parcelable {
    var id: String = UUID.randomUUID().toString()
    var ordinal: Int = 0
    var carbsPerUnit: Int = 0
    var startTime: Int = 0

    private constructor(parcel: Parcel) : this() {
        id = parcel.readString()

        ordinal = parcel.readInt()
        ordinal = ordinal

        carbsPerUnit = parcel.readInt()
        carbsPerUnit = ordinal

        startTime = parcel.readInt()
        startTime = ordinal
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(id)
        dest?.writeInt(ordinal)
        dest?.writeInt(carbsPerUnit)
        dest?.writeInt(startTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        public val CREATOR = object: Parcelable.Creator<BolusRateParcelable> {
            override fun createFromParcel(parcel: Parcel): BolusRateParcelable {
                return BolusRateParcelable(parcel)
            }

            override fun newArray(size: Int): Array<BolusRateParcelable> {
                return Array(size, {i -> BolusRateParcelable() })
            }
        }
    }
}