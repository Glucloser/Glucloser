package com.nlefler.glucloser.a.models.parcelable

import android.os.Parcel
import android.os.Parcelable
import com.nlefler.glucloser.a.models.BloodSugar
import com.nlefler.glucloser.a.models.BloodSugarEntity
import com.nlefler.glucloser.a.models.BolusPattern
import com.nlefler.glucloser.a.models.Meal
import com.nlefler.glucloser.a.models.parcelable.BloodSugarParcelable
import com.nlefler.glucloser.a.models.parcelable.BolusPatternParcelable
import java.util.*

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class MealParcelable() :  Parcelable {

    var placeParcelable: PlaceParcelable? = null
    var primaryId: String = UUID.randomUUID().toString()
    var date: Date = Date()
    var bolusPatternParcelable: BolusPatternParcelable? = null
    var carbs: Int = 0
    var insulin: Float = 0f
    var bloodSugarParcelable: BloodSugarParcelable? = null
    var isCorrection: Boolean = false
    var foodParcelables: MutableList<FoodParcelable> = ArrayList<FoodParcelable>()

    /** Parcelable  */
    protected constructor(parcel: Parcel): this() {
        primaryId = parcel.readString()
        placeParcelable = parcel.readParcelable<PlaceParcelable>(PlaceParcelable::class.java.classLoader)
        carbs = parcel.readInt()
        insulin = parcel.readFloat()
        isCorrection = parcel.readInt() != 0
        bloodSugarParcelable = parcel.readParcelable<BloodSugarParcelable>(BloodSugarEntity::class.java.classLoader)
        val time = parcel.readLong()
        if (time > 0) {
            date = Date(time)
        }
        bolusPatternParcelable = parcel.readParcelable<BolusPatternParcelable>(BolusPatternParcelable::class.java.classLoader)
        parcel.readTypedList(this.foodParcelables, FoodParcelable.CREATOR)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(primaryId)
        dest.writeParcelable(placeParcelable, flags)
        dest.writeInt(carbs)
        dest.writeFloat(insulin)
        dest.writeInt(if (isCorrection) 1 else 0)
        dest.writeParcelable(bloodSugarParcelable, flags)
        dest.writeLong(date.time)
        dest.writeParcelable(bolusPatternParcelable, flags)
        dest.writeTypedList(this.foodParcelables)
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<MealParcelable> = object : Parcelable.Creator<MealParcelable> {
            override fun createFromParcel(parcel: Parcel): MealParcelable {
                return MealParcelable(parcel)
            }

            override fun newArray(size: Int): Array<MealParcelable> {
                return Array(size, {i -> MealParcelable() })
            }
        }
    }
}
