package com.nlefler.glucloser.actions

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import bolts.Continuation
import bolts.Task
import bolts.TaskCompletionSource

import com.nlefler.glucloser.dataSource.*
import com.nlefler.glucloser.models.*
import com.nlefler.glucloser.models.parcelable.*

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
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

    lateinit var realmManager: RealmManager
        @Inject set

    // TODO(nl) Sync bolus events to server
//    lateinit var parseUploader: ParseUploader
//        @Inject set


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

        var beforeSugarTask: Task<BloodSugar?>? = Task.forResult(null)
        var beforeSugarParcelable = bolusEventParcelable?.bloodSugarParcelable;
        if (beforeSugarParcelable != null) {
            beforeSugarTask = bloodSugarFactory.bloodSugarFromParcelable(beforeSugarParcelable)
        }

        val foodTasks = ArrayList<Task<Food?>>()
        val foodList = RealmList<Food>()
        for (foodParcelable in this.foodParcelableList) {
            foodTasks.add(foodFactory.foodFromParcelable(foodParcelable).continueWithTask(Continuation<Food?, Task<Food?>> foodC@ { task ->
                if (task.isFaulted) {
                    return@foodC task
                }

                val food = task.result
                foodList.add(food)
                return@foodC Task.forResult(food)
            }))
        }

        var bolusPatternTask: Task<BolusPattern?>? = Task.forResult(null)
        if (bolusEventParcelable?.bolusPatternParcelable != null) {
            bolusPatternTask = bolusPatternFactory.bolusPatternFromParcelable(bolusEventParcelable?.bolusPatternParcelable!!)
        }

        val allTasks = arrayListOf(beforeSugarTask, bolusPatternTask)
        allTasks.addAll(foodTasks)
        Task.whenAll(allTasks).continueWith all@ { task ->
            if (task.isFaulted) {
                return@all
            }

            when (this.bolusEventParcelable ?: null) {
                is MealParcelable -> {
                    var placeTask: Task<Place?>? = Task.forResult(null)
                    if (this.placeParcelable != null) {
                        placeTask = placeFactory.placeFromParcelable(this.placeParcelable!!)
                    }

                    val mealTask = mealFactory.mealFromParcelable(this.bolusEventParcelable as MealParcelable)

                    Task.whenAll(arrayListOf(mealTask, placeTask)).continueWithTask(Continuation<Void, Task<Meal?>> mealAll@ { task ->
                        if (task.isFaulted) {
                            return@mealAll Task.forError<Meal?>(task.error)
                        }

                        val meal = mealTask.result
                        val place = placeTask?.result

                        return@mealAll realmManager.executeTransaction(object: RealmManager.Tx<Meal?> {
                            override fun dependsOn(): List<RealmObject?> {
                                return listOf(meal, place)
                            }

                            override fun execute(dependsOn: List<RealmObject?>, realm: Realm): Meal? {
                                val liveMeal = dependsOn.first() as Meal?
                                val livePlace = dependsOn.last() as Place?

                                liveMeal?.foods = foodList

                                if (livePlace != null) {
                                    liveMeal?.place = livePlace
                                }

                                liveMeal?.beforeSugar = beforeSugarTask?.result
                                liveMeal?.bolusPattern = bolusPatternTask?.result
                                return liveMeal
                            }

                        })

                    }).continueWith mealUpload@ { task ->
                        if (task.isFaulted || task.result == null || task.result !is Meal) {
                            return@mealUpload
                        }

//                        parseUploader.uploadBolusEvent(task.result as Meal)
                    }

                }
                is SnackParcelable -> {
                    snackFactory.snackFromParcelable(this.bolusEventParcelable as SnackParcelable)
                            .continueWithTask(Continuation<Snack?, Task<Snack?>> snackPar@ { task ->
                        if (task.isFaulted) {
                            return@snackPar task
                        }

                        val snack = task.result
                        return@snackPar realmManager.executeTransaction(object: RealmManager.Tx<Snack?> {
                            override fun dependsOn(): List<RealmObject?> {
                                return listOf(snack)
                            }

                            override fun execute(dependsOn: List<RealmObject?>, realm: Realm): Snack? {
                                val liveSnack = dependsOn.first() as Snack?

                                liveSnack?.foods = foodList
                                liveSnack?.beforeSugar = beforeSugarTask?.result
                                liveSnack?.bolusPattern = bolusPatternTask?.result
                                return liveSnack
                            }
                        })
                    }).continueWith { task ->
                        if (task.isFaulted || task.result == null || task.result !is Snack) {
                            return@continueWith
                        }

//                        parseUploader.uploadBolusEvent(task.result as Snack)
                    }
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
