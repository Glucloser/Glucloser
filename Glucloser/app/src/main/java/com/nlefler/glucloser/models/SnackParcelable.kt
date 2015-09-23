package com.nlefler.glucloser.models

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Created by Nathan Lefler on 5/8/15.
 */
public data class SnackParcelable() : Parcelable, BolusEventParcelable {

    override var id: String = UUID.randomUUID().toString()
    override var date: Date = Date()
    override var bolusPatternParcelable: BolusPatternParcelable? = null
    override var carbs: Int = 0
    override var insulin: Float = 0f
    override var bloodSugarParcelable: BloodSugarParcelable? = null
    override var isCorrection: Boolean = false
    override var foodParcelables: List<FoodParcelable> = ArrayList<FoodParcelable>()

    /** Parcelable  */
    protected constructor(parcel: Parcel): this() {
        id = parcel.readString()
        carbs = parcel.readInt()
        insulin = parcel.readFloat()
        isCorrection = parcel.readInt() != 0
        bloodSugarParcelable = parcel.readParcelable<BloodSugarParcelable>(BloodSugar::class.java.classLoader)
        val time = parcel.readLong()
        if (time > 0) {
            date = Date()
        }
        bolusPatternParcelable = parcel.readParcelable<BolusPatternParcelable>(BolusPatternParcelable::class.java.classLoader)
        parcel.readList(this.foodParcelables, FoodParcelable::class.java.classLoader)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeInt(carbs)
        dest.writeFloat(insulin)
        dest.writeInt(if (isCorrection) 1 else 0)
        dest.writeParcelable(bloodSugarParcelable, flags)
        dest.writeLong(date.time)
        dest.writeParcelable(bolusPatternParcelable, 0)
        dest.writeTypedList(this.foodParcelables)
    }

    companion object {

        public val CREATOR: Parcelable.Creator<SnackParcelable> = object : Parcelable.Creator<SnackParcelable> {
            override fun createFromParcel(parcel: Parcel): SnackParcelable {
                return SnackParcelable(parcel)
            }

            override fun newArray(size: Int): Array<SnackParcelable> {
                return Array(size, {i -> SnackParcelable()})
            }
        }
    }
}
