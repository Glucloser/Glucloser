package com.nlefler.glucloser.a.models.parcelable

import android.os.Parcel
import android.os.Parcelable
import com.nlefler.glucloser.a.models.Food
import com.nlefler.glucloser.a.models.Meal

import java.util.UUID


/**
 * Created by Nathan Lefler on 5/16/15.
 */
class FoodParcelable() : Food, Parcelable {
    override var primaryId: String = UUID.randomUUID().toString()
    override var carbs: Int = 0
    override var foodName: String = ""
    override var insulin: Float = 0f

    // TODO(nl): for requery
    override var meal: Meal? = null

    /** Parcelable  */
    protected constructor(`in`: Parcel): this() {
        primaryId = `in`.readString()
        carbs = `in`.readInt()
        foodName = `in`.readString()
        insulin = `in`.readFloat()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(primaryId)
        dest.writeInt(carbs)
        dest.writeString(foodName)
        dest.writeFloat(insulin)
    }

    override fun equals(other: Any?): Boolean{
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as FoodParcelable

        if (primaryId!= other.primaryId) return false

        return true
    }

    override fun hashCode(): Int{
        return primaryId.hashCode()
    }


    companion object {

        @SuppressWarnings("unused")
        @JvmField val CREATOR: Parcelable.Creator<Food> = object : Parcelable.Creator<Food> {
            override fun createFromParcel(`in`: Parcel): FoodParcelable {
                return FoodParcelable(`in`)
            }

            override fun newArray(size: Int): Array<Food> {
                return Array(size, {i -> FoodParcelable() })
            }
        }
    }
}
