package com.nlefler.glucloser.models

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by nathan on 9/20/15.
 */
public data class BolusRateParcelable(): Parcelable {
    public var ordinal: Int? = null
    public var rate: Int? = null
    public var startTime: Int? = null

    private constructor(parcel: Parcel) : this() {
        ordinal = parcel.readInt()
        ordinal = if (ordinal?:-1 > 0) ordinal else null

        rate = parcel.readInt()
        rate = if (rate?:-1 > 0) rate else null

        startTime = parcel.readInt()
        startTime = if (startTime?:-1 > 0) startTime else null
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(ordinal ?: -1)
        dest?.writeInt(rate ?: -1)
        dest?.writeInt(startTime ?: -1)
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
                return Array(size, {i -> BolusRateParcelable()})
            }
        }
    }
}