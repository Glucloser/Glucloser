package com.nlefler.glucloser.models.parcelable

import android.os.Parcel
import android.os.Parcelable
import com.nlefler.glucloser.models.parcelable.BolusRateParcelable
import java.util.*

/**
 * Created by nathan on 9/20/15.
 */
public class BolusPatternParcelable(): Parcelable {
    public var id: String? = null
    public var rateCount: Int? = null
    public var rates: MutableList<BolusRateParcelable> = ArrayList<BolusRateParcelable>()

    private constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        rateCount = parcel.readInt()
        parcel.readTypedList<BolusRateParcelable>(rates, BolusRateParcelable.CREATOR)
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(id ?: "")
        dest?.writeInt(rateCount ?: 0)
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
