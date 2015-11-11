package com.nlefler.glucloser.actions

import android.os.Parcel
import android.os.Parcelable
import android.util.Log

import com.nlefler.glucloser.dataSource.*
import com.nlefler.glucloser.models.*

import io.realm.Realm
import io.realm.RealmList
import java.util.ArrayList
import javax.inject.Inject

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class LogBolusEventAction : Parcelable {
    @Inject public var bolusPatternFactory: BolusPatternFactory? = null
    @Inject public var mealFactory: MealFactory? = null
    @Inject public var bloodSugarFactory: BloodSugarFactory? = null
    @Inject public var foodFactory: FoodFactory? = null
    @Inject public var placeFactory: PlaceFactory? = null
    @Inject public var snackFactory: SnackFactory? = null
    @Inject public var realm: Realm? = null
    @Inject public var parseUploader: ParseUploader? = null

    private var placeParcelable: PlaceParcelable? = null
    private var bolusEventParcelable: BolusEventParcelable? = null
    private val foodParcelableList: MutableList<FoodParcelable> = ArrayList<FoodParcelable>()

    public constructor() {
    }

    public fun setPlaceParcelable(placeParcelable: PlaceParcelable) {
        this.placeParcelable = placeParcelable
    }

    public fun setBolusEventParcelable(bolusEventParcelable: BolusEventParcelable) {
        this.bolusEventParcelable = bolusEventParcelable
    }

    public fun addFoodParcelable(foodParcelable: FoodParcelable) {
        this.foodParcelableList.add(foodParcelable)
    }

    public fun log() {
        if (this.bolusEventParcelable == null) {
            Log.e(LOG_TAG, "Can't log bolus event, bolus event is null")
            return
        }

        var beforeSugar: BloodSugar? = null
        var beforeSugarParcelable = bolusEventParcelable?.bloodSugarParcelable;
        if (beforeSugarParcelable != null) {
            beforeSugar = bloodSugarFactory?.bloodSugarFromParcelable(beforeSugarParcelable)
        }

        val foodList = RealmList<Food>()
        for (foodParcelable in this.foodParcelableList) {
            val food = foodFactory?.foodFromParcelable(foodParcelable)
            foodList.add(food)
        }

        val bolusPattern = bolusPatternFactory?.bolusPatternFromParcelable(bolusEventParcelable?.bolusPatternParcelable!!)

        when (this.bolusEventParcelable) {
            is MealParcelable -> {
                val meal = mealFactory?.mealFromParcelable(this.bolusEventParcelable as MealParcelable)

                var place: Place? = null
                if (this.placeParcelable != null) {
                    place = placeFactory?.placeFromParcelable(this.placeParcelable!!)
                }
                realm?.beginTransaction()
                meal?.foods = foodList

                if (place != null) {
                    meal?.place = place
                }

                meal?.beforeSugar = beforeSugar
                meal?.bolusPattern = bolusPattern

                realm?.commitTransaction()
                if (meal != null) {
                    parseUploader?.uploadBolusEvent(meal)
                }
            }
            is SnackParcelable -> {
                val snack = snackFactory?.snackFromParcelable(this.bolusEventParcelable as SnackParcelable)
                realm?.beginTransaction()

                snack?.foods = foodList
                snack?.beforeSugar = beforeSugar
                snack?.bolusPattern = bolusPattern

                realm?.commitTransaction()
                if (snack != null) {
                    parseUploader?.uploadBolusEvent(snack)
                }
            }
        }
    }

    /** Parcelable  */
    public constructor(parcel: Parcel) {
        this.placeParcelable = parcel.readParcelable<Parcelable>(PlaceParcelable::class.java.classLoader) as PlaceParcelable

        val eventTypeName = parcel.readString()
        when (try {BolusEventType.valueOf(eventTypeName) } catch (e: Exception) { null }) {
            BolusEventType.BolusEventTypeMeal -> {
                this.bolusEventParcelable = parcel.readParcelable<MealParcelable>(MealParcelable::class.java.classLoader)
            }
            BolusEventType.BolusEventTypeSnack -> {
                this.bolusEventParcelable = parcel.readParcelable<SnackParcelable>(SnackParcelable::class.java.classLoader)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeParcelable(this.placeParcelable, flags)
        when (this.bolusEventParcelable) {
            is MealParcelable -> {
                out.writeString(BolusEventType.BolusEventTypeMeal.name)
            }
            is SnackParcelable -> {
                out.writeString(BolusEventType.BolusEventTypeSnack.name)
            }
        }
        out.writeParcelable(this.bolusEventParcelable as Parcelable, flags)
    }

    companion object {
        private val LOG_TAG = "LogMealAction"

        public val CREATOR = object : Parcelable.Creator<LogBolusEventAction> {
            override fun createFromParcel(parcel: Parcel): LogBolusEventAction {
                return LogBolusEventAction(parcel)
            }

            override fun newArray(size: Int): Array<LogBolusEventAction?> {
                return Array(size, {i -> LogBolusEventAction() })
            }
        }
    }
}
