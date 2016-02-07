package com.nlefler.glucloser.models.parcelable

import android.os.Parcel
import android.os.Parcelable
import com.nlefler.glucloser.models.parcelable.BolusRateParcelable
import java.util.*

/**
 * Created by nathan on 9/20/15.
 */
public class BolusPatternParcelable(): Parcelable {
    public var id: String = UUID.randomUUID().toString()
    public var rateCount: Int = 0
    public var rates: MutableList<BolusRateParcelable> = ArrayList()

    private constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        rateCount = parcel.readInt()
        parcel.readTypedList<BolusRateParcelable>(rates, BolusRateParcelable.CREATOR)
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(id)
        dest?.writeInt(rateCount)
        dest?.writeTypedList<BolusRateParcelable>(rates)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        public val CREATOR = object: Parcelable.Creator<BolusPatternParcelable> {
            override fun createFromParcel(parcelIn: Parcel): BolusPatternParcelable {
                return BolusPatternParcelable(parcelIn)
            }

            override fun newArray(size: Int): Array<BolusPatternParcelable> {
                return Array(size, {i -> BolusPatternParcelable() })
            }
        }
    }
}
