package com.nlefler.glucloser.a.models.parcelable

import android.os.Parcel
import android.os.Parcelable

import java.util.UUID


/**
 * Created by Nathan Lefler on 5/16/15.
 */
class FoodParcelable : Parcelable {
    var foodId: String = UUID.randomUUID().toString()
    var carbs: Int? = null
    var foodName: String? = null

    constructor() {
    }

    /** Parcelable  */
    protected constructor(`in`: Parcel) {
        foodId = `in`.readString()
        carbs = `in`.readString()?.toInt()
        foodName = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(foodId)
        dest.writeString(carbs.toString())
        dest.writeString(foodName)
    }

    companion object {

        @SuppressWarnings("unused")
        @JvmField val CREATOR: Parcelable.Creator<FoodParcelable> = object : Parcelable.Creator<FoodParcelable> {
            override fun createFromParcel(`in`: Parcel): FoodParcelable {
                return FoodParcelable(`in`)
            }

            override fun newArray(size: Int): Array<FoodParcelable> {
                return Array(size, {i -> FoodParcelable() })
            }
        }
    }
}
