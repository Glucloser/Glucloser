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
    lateinit var bolusPatternFactory: BolusPatternFactory
        @Inject set

    lateinit var mealFactory: MealFactory
        @Inject set

    lateinit var bloodSugarFactory: BloodSugarFactory
        @Inject set

    lateinit var foodFactory: FoodFactory
        @Inject set

    lateinit var placeFactory: PlaceFactory
        @Inject set

    lateinit var snackFactory: SnackFactory
        @Inject set

    lateinit var realm: Realm
        @Inject set

    lateinit var parseUploader: ParseUploader
        @Inject set


    private var placeParcelable: PlaceParcelable? = null
    private var bolusEventParcelable: BolusEventParcelable? = null
    private val foodParcelableList: MutableList<FoodParcelable> = ArrayList()

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
            beforeSugar = bloodSugarFactory.bloodSugarFromParcelable(beforeSugarParcelable)
        }

        val foodList = RealmList<Food>()
        for (foodParcelable in this.foodParcelableList) {
            val food = foodFactory.foodFromParcelable(foodParcelable)
            foodList.add(food)
        }

        bolusPatternFactory.bolusPatternFromParcelable(bolusEventParcelable?.bolusPatternParcelable!!).continueWith { task ->
            val bolusPattern = task.result
            when (this.bolusEventParcelable ?: null) {
                is MealParcelable -> {
                    mealFactory.mealFromParcelable(this.bolusEventParcelable as MealParcelable).continueWith { task ->
                        var place: Place? = null
                        if (this.placeParcelable != null) {
                            place = placeFactory.placeFromParcelable(this.placeParcelable!!)
                        }
                        val meal = task.result
                        realm.executeTransaction {
                            meal.foods = foodList

                            if (place != null) {
                                meal.place = place
                            }

                            meal.beforeSugar = beforeSugar
                            meal.bolusPattern = bolusPattern

                            parseUploader.uploadBolusEvent(meal)
                        }

                    }

                }
                is SnackParcelable -> {
                    val snack = snackFactory.snackFromParcelable(this.bolusEventParcelable as SnackParcelable)
                    realm.beginTransaction()

                    snack.foods = foodList
                    snack.beforeSugar = beforeSugar
                    snack.bolusPattern = bolusPattern

                    realm.commitTransaction()
                    parseUploader.uploadBolusEvent(snack)
                }
                else -> {
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
