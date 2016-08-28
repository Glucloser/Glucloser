package com.nlefler.glucloser.a.models.parcelable

import android.os.Parcel
import android.os.Parcelable
import com.nlefler.glucloser.a.models.BolusPattern
import com.nlefler.glucloser.a.models.BolusRate
import java.util.*

/**
 * Created by nathan on 9/20/15.
 */
public class BolusRateParcelable(): BolusRate, Parcelable {
    override var primaryId: String = UUID.randomUUID().toString()
    override var ordinal: Int = 0
    override var carbsPerUnit: Int = 0
    override var startTime: Int = 0
    // TODO(nl): required by requery?
    override var pattern: BolusPattern? = null

    private constructor(parcel: Parcel) : this() {
        primaryId = parcel.readString()

        ordinal = parcel.readInt()
        ordinal = ordinal

        carbsPerUnit = parcel.readInt()
        carbsPerUnit = ordinal

        startTime = parcel.readInt()
        startTime = ordinal
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(primaryId)
        dest?.writeInt(ordinal)
        dest?.writeInt(carbsPerUnit)
        dest?.writeInt(startTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField val CREATOR = object: Parcelable.Creator<BolusRate> {
            override fun createFromParcel(parcel: Parcel): BolusRateParcelable {
                return BolusRateParcelable(parcel)
            }

            override fun newArray(size: Int): Array<BolusRate> {
                return Array(size, {i -> BolusRateParcelable() })
            }
        }
    }
}