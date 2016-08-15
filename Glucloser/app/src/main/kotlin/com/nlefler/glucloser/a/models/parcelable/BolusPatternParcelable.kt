package com.nlefler.glucloser.a.models.parcelable

import android.os.Parcel
import android.os.Parcelable
import com.nlefler.glucloser.a.models.parcelable.BolusRateParcelable
import java.util.*

/**
 * Created by nathan on 9/20/15.
 */
public class BolusPatternParcelable(): Parcelable {
    var id: String = UUID.randomUUID().toString()
    var updatedOn: Date? = null
    var rateCount: Int = 0
    var rates: MutableList<BolusRateParcelable> = ArrayList()

    private constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        val time = parcel.readLong()
        if (time > 0) {
            updatedOn = Date(parcel.readLong())
        }
        rateCount = parcel.readInt()
        parcel.readTypedList<BolusRateParcelable>(rates, BolusRateParcelable.CREATOR)
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(id)
        dest?.writeLong(updatedOn?.time ?: 0)
        dest?.writeInt(rateCount)
        dest?.writeTypedList<BolusRateParcelable>(rates)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField val CREATOR = object: Parcelable.Creator<BolusPatternParcelable> {
            override fun createFromParcel(parcelIn: Parcel): BolusPatternParcelable {
                return BolusPatternParcelable(parcelIn)
            }

            override fun newArray(size: Int): Array<BolusPatternParcelable> {
                return Array(size, {i -> BolusPatternParcelable() })
            }
        }
    }
}
