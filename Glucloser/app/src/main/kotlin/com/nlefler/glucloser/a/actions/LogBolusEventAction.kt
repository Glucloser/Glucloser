package com.nlefler.glucloser.a.actions

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import bolts.Continuation
import bolts.Task
import bolts.TaskCompletionSource

import com.nlefler.glucloser.a.dataSource.*
import com.nlefler.glucloser.a.models.*
import com.nlefler.glucloser.a.models.parcelable.*
import com.nlefler.glucloser.a.dataSource.*
import com.nlefler.glucloser.a.dataSource.sync.cairo.CairoServices
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoCollectionService
import com.nlefler.glucloser.a.db.RealmManager
import com.nlefler.glucloser.a.models.*
import com.nlefler.glucloser.a.models.parcelable.*

import java.util.ArrayList
import javax.inject.Inject

/**
 * Created by Nathan Lefler on 12/24/14.
 */
class LogBolusEventAction : Parcelable {
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

    lateinit var realmManager: RealmManager
        @Inject set

    lateinit var cairoServices: CairoServices
        @Inject set


    private var placeParcelable: PlaceParcelable? = null
    private var bolusEventParcelable: BolusEventParcelable? = null
    private val foodParcelableList: MutableList<FoodParcelable> = ArrayList()

    constructor() {
    }

    fun setPlaceParcelable(placeParcelable: PlaceParcelable) {
        this.placeParcelable = placeParcelable
    }

    fun setBolusEventParcelable(bolusEventParcelable: BolusEventParcelable) {
        this.bolusEventParcelable = bolusEventParcelable
    }

    fun addFoodParcelable(foodParcelable: FoodParcelable) {
        this.foodParcelableList.add(foodParcelable)
    }

    fun log() {
        assert(bolusEventParcelable != null)
        val savingBolusEventParcel = bolusEventParcelable

        var beforeSugarTask: Task<BloodSugar?>? = Task.forResult(null)
        val beforeSugarParcelable = bolusEventParcelable?.bloodSugarParcelable
        if (beforeSugarParcelable != null) {
            beforeSugarTask = bloodSugarFactory.bloodSugarFromParcelable(beforeSugarParcelable)
        }

        val foodTasks = ArrayList<Task<Food?>>()
        val foodList = ArrayList<Food>()
        for (foodParcelable in this.foodParcelableList) {
            foodTasks.add(foodFactory.foodFromParcelable(foodParcelable).continueWithTask(Continuation<Food?, Task<Food?>> foodC@ { task ->
                if (task.isFaulted) {
                    return@foodC task
                }

                val food = task.result
                if (food == null) {
                    return@foodC Task.forError(Exception("Food should not be nil"))
                }
                foodList.add(food)
                return@foodC Task.forResult(food)
            }))
        }

        var bolusPatternTask: Task<BolusPattern?>? = Task.forResult(null)
        val bolusPatternParcel = bolusEventParcelable?.bolusPatternParcelable
        if (bolusPatternParcel != null) {
            bolusPatternTask = bolusPatternFactory.bolusPatternFromParcelable(bolusPatternParcel)
        }

        val allTasks = listOf(beforeSugarTask, bolusPatternTask) + foodTasks
        Task.whenAll(allTasks).continueWith all@ { task ->
            if (task.isFaulted) {
                return@all
            }

            val beforeSugar = beforeSugarTask?.result
            val bolusPattern = bolusPatternTask?.result

            when (savingBolusEventParcel) {
                is MealParcelable -> {
                    assert(placeParcelable != null)

                    val placeTask = placeFactory.placeFromParcelable(this.placeParcelable!!)
                    val mealTask = mealFactory.mealFromParcelable(this.bolusEventParcelable as MealParcelable)

                    Task.whenAll(arrayListOf(mealTask, placeTask)).continueWithTask(Continuation<Void, Task<Meal?>> mealAll@ { task ->
                        if (task.isFaulted) {
                            return@mealAll Task.forError<Meal?>(task.error)
                        }

                        val meal = mealTask.result
                        val place = placeTask.result

                        if (meal == null || bolusPattern == null || foodList.size == 0) {
                            Log.e(LOG_TAG, "meal: $meal Bolus Pattern: $bolusPattern Foods: $foodList")
                            return@mealAll Task.forError(Exception("Dependencies null"))
                        }

                        return@mealAll realmManager.executeTransaction(object : RealmManager.Tx<Meal?> {
                            override fun dependsOn(): List<RealmObject?> {
                                return listOf(meal, place, beforeSugar, bolusPattern) + foodList
                            }

                            override fun execute(dependsOn: List<RealmObject?>, realm: Realm): Meal? {
                                val liveMeal = dependsOn.first() as Meal?
                                val livePlace = dependsOn[1] as Place?
                                val liveSugar = dependsOn[2] as BloodSugar?
                                val liveBolusPattern = dependsOn[3] as BolusPattern?
                                val liveFoods = dependsOn.slice(IntRange(4, dependsOn.size - 1)) as List<Food>

                                liveMeal?.foods?.addAll(liveFoods)

                                if (livePlace != null) {
                                    liveMeal?.place = livePlace
                                }

                                liveMeal?.beforeSugar = liveSugar
                                liveMeal?.bolusPattern = liveBolusPattern

                                val visitCount = livePlace?.visitCount ?: 0
                                livePlace?.visitCount = visitCount + 1

                                return liveMeal
                            }

                        })

                    }).continueWith mealUpload@ { task ->
                        val meal = task.result
                        if (task.isFaulted || meal == null) {
                            return@mealUpload
                        }
                        val place = meal.place
                        if (place != null) {
                            cairoServices.collectionService().addPlace(place)
                        }
                        cairoServices.collectionService().addMeal(meal)
                    }

                }
                is SnackParcelable -> {
                    snackFactory.snackFromParcelable(this.bolusEventParcelable as SnackParcelable)
                            .continueWithTask(Continuation<Snack?, Task<Snack?>> snackPar@ { task ->

                                if (task.isFaulted) {
                                    return@snackPar task
                                }

                                val snack = task.result

                                if (snack == null || bolusPattern == null || foodList.size == 0) {
                                    Log.e(LOG_TAG, "Snack: $snack Bolus Pattern: $bolusPattern Foods: $foodList")
                                    return@snackPar Task.forError(Exception("Dependencies null"))
                                }

                                return@snackPar realmManager.executeTransaction(object : RealmManager.Tx<Snack?> {
                                    override fun dependsOn(): List<RealmObject?> {
                                        return listOf(snack, beforeSugar, bolusPattern) + foodList
                                    }

                                    override fun execute(dependsOn: List<RealmObject?>, realm: Realm): Snack? {
                                        val liveSnack = dependsOn.first() as Snack?
                                        val liveSugar = dependsOn[1] as BloodSugar?
                                        val liveBolusPattern = dependsOn[2] as BolusPattern?
                                        val liveFoods = dependsOn.slice(IntRange(3, dependsOn.count() - 1)) as List<Food>

                                        liveSnack?.foods?.addAll(liveFoods)
                                        liveSnack?.beforeSugar = liveSugar
                                        liveSnack?.bolusPattern = liveBolusPattern
                                        return liveSnack
                                    }
                                })
                            }).continueWith { task ->
                        val snack = task.result
                        if (task.isFaulted || snack == null) {
                            return@continueWith
                        }

                        cairoServices.collectionService().addSnack(snack)
                    }
                }
                else -> {
                }
            }
        }

    }

    /** Parcelable  */
    constructor(parcel: Parcel) {
        this.placeParcelable = parcel.readParcelable<Parcelable>(PlaceParcelable::class.java.classLoader) as PlaceParcelable

        val eventTypeName = parcel.readString()
        when (try {
            BolusEventType.valueOf(eventTypeName) } catch (e: Exception) { null }) {
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

        val CREATOR = object : Parcelable.Creator<LogBolusEventAction> {
            override fun createFromParcel(parcel: Parcel): LogBolusEventAction {
                return LogBolusEventAction(parcel)
            }

            override fun newArray(size: Int): Array<LogBolusEventAction?> {
                return Array(size, {i -> LogBolusEventAction() })
            }
        }
    }
}
