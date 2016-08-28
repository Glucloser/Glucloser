package com.nlefler.glucloser.a.models.parcelable

import android.os.Parcel
import android.os.Parcelable
import com.nlefler.glucloser.a.GlucloserApplication
import com.nlefler.glucloser.a.dataSource.BolusRateFactory
import com.nlefler.glucloser.a.models.BolusPattern
import com.nlefler.glucloser.a.models.BolusRate
import com.nlefler.glucloser.a.models.parcelable.BolusRateParcelable
import java.util.*
import javax.inject.Inject

/**
 * Created by nathan on 9/20/15.
 */
class BolusPatternParcelable @Inject constructor(val bolusRateFactory: BolusRateFactory): BolusPattern, Parcelable {
    override var primaryId: String = UUID.randomUUID().toString()
    override var updatedOn: Date = Date()
    override var rates: MutableList<BolusRate> = ArrayList()

    // TODO(nl): bleh
    private constructor(parcel: Parcel) : this({
        GlucloserApplication.sharedApplication?.rootComponent?.bolusRateFactory() as BolusRateFactory
    }()) {
        primaryId = parcel.readString()
        val time = parcel.readLong()
        if (time > 0) {
            updatedOn = Date(parcel.readLong())
        }
        parcel.readTypedList<BolusRate>(rates, BolusRateParcelable.CREATOR)
    }

        override fun writeToParcel(dest: Parcel?, flags: Int) {
            dest?.writeString(primaryId)
            dest?.writeLong(updatedOn.time ?: 0)
            dest?.writeTypedList<BolusRateParcelable>(rates.map { r -> bolusRateFactory.parcelableFromBolusRate(r) })
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object {
        @JvmField val CREATOR = object: Parcelable.Creator<BolusPattern> {
            override fun createFromParcel(parcelIn: Parcel): BolusPatternParcelable {
                return BolusPatternParcelable(parcelIn)
            }

            override fun newArray(size: Int): Array<BolusPattern> {
                // TODO(nl): hmmm
                val brf = GlucloserApplication.sharedApplication?.rootComponent?.bolusRateFactory() as BolusRateFactory
                return Array(size, {i -> BolusPatternParcelable(brf) })
            }
        }
    }
}
