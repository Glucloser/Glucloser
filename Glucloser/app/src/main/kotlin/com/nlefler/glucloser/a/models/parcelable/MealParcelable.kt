package com.nlefler.glucloser.a.models.parcelable

import android.os.Parcel
import android.os.Parcelable
import com.nlefler.glucloser.a.GlucloserApplication
import com.nlefler.glucloser.a.dataSource.BloodSugarFactory
import com.nlefler.glucloser.a.dataSource.BolusPatternFactory
import com.nlefler.glucloser.a.dataSource.FoodFactory
import com.nlefler.glucloser.a.dataSource.PlaceFactory
import com.nlefler.glucloser.a.models.*
import com.nlefler.glucloser.a.models.parcelable.BloodSugarParcelable
import com.nlefler.glucloser.a.models.parcelable.BolusPatternParcelable
import java.util.*
import javax.inject.Inject

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class MealParcelable @Inject constructor(val placeFactory: PlaceFactory,
                                                val bloodSugarFactory: BloodSugarFactory,
                                                val bolusPatternFactory: BolusPatternFactory,
                                                val foodFactory: FoodFactory) : Meal, Parcelable {

    override var place: Place? = null
    override var primaryId: String = UUID.randomUUID().toString()
    override var eatenDate: Date = Date()
    override var bolusPattern: BolusPattern? = null
    override var carbs: Int = 0
    override var insulin: Float = 0f
    override var beforeSugar: BloodSugar? = BloodSugarParcelable()
    override var isCorrection: Boolean = false
    override var foods: MutableSet<Food> = HashSet()
    override var needsUpload: Boolean = false

    /** Parcelable  */
    protected constructor(parcel: Parcel): this({
        // TODO(nl): Eh
        GlucloserApplication.sharedApplication?.rootComponent?.placeFactory() as PlaceFactory
    }(),
    {
        // TODO(nl): Eh
        GlucloserApplication.sharedApplication?.rootComponent?.bloodSugarFactory() as BloodSugarFactory
    }(),
    {
        // TODO(nl): Eh
        GlucloserApplication.sharedApplication?.rootComponent?.bolusPatternFactory() as BolusPatternFactory
    }(),
    {
        // TODO(nl): Eh
        GlucloserApplication.sharedApplication?.rootComponent?.foodFactory() as FoodFactory
    }()) {
        primaryId = parcel.readString()
        place = parcel.readParcelable<PlaceParcelable>(PlaceParcelable::class.java.classLoader)
        carbs = parcel.readInt()
        insulin = parcel.readFloat()
        isCorrection = parcel.readInt() != 0
        beforeSugar = parcel.readParcelable<BloodSugarParcelable>(BloodSugarEntity::class.java.classLoader)
        val time = parcel.readLong()
        if (time > 0) {
            eatenDate = Date(time)
        }
        bolusPattern = parcel.readParcelable<BolusPatternParcelable>(BolusPatternParcelable::class.java.classLoader)
        val foodsList = ArrayList<Food>()
        parcel.readTypedList(foodsList, FoodParcelable.CREATOR)
        foods.addAll(foodsList)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(primaryId)

        val p = place
        if (p != null) {
            dest.writeParcelable(placeFactory.parcelableFrom(p), flags)
        }
        else {
            dest.writeValue(null)
        }
        dest.writeInt(carbs)
        dest.writeFloat(insulin)
        dest.writeInt(if (isCorrection) 1 else 0)

        val bs = beforeSugar
        if (bs != null) {
            dest.writeParcelable(bloodSugarFactory.parcelableFromBloodSugar(bs), flags)
        }
        else {
            dest.writeValue(null)
        }
        dest.writeLong(eatenDate.time)

        val bp = bolusPattern
        if (bp != null) {
            dest.writeParcelable(bolusPatternFactory.parcelableFrom(bp), flags)
        }
        else {
            dest.writeValue(null)
        }
        dest.writeTypedList(this.foods.map { f -> foodFactory.parcelableFrom(f) })
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<MealParcelable> = object : Parcelable.Creator<MealParcelable> {
            override fun createFromParcel(parcel: Parcel): MealParcelable {
                return MealParcelable(parcel)
            }

            override fun newArray(size: Int): Array<MealParcelable> {
                // TODO(nl): Eh
                val pf = GlucloserApplication.sharedApplication?.rootComponent?.placeFactory() as PlaceFactory
                val bsf = GlucloserApplication.sharedApplication?.rootComponent?.bloodSugarFactory() as BloodSugarFactory
                val bpf = GlucloserApplication.sharedApplication?.rootComponent?.bolusPatternFactory() as BolusPatternFactory
                val ff = GlucloserApplication.sharedApplication?.rootComponent?.foodFactory() as FoodFactory
                return Array(size, {i -> MealParcelable(pf, bsf, bpf, ff) })
            }
        }
    }
}
