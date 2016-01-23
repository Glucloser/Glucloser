package com.nlefler.glucloser.actions

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import bolts.Continuation
import bolts.Task
import bolts.TaskCompletionSource

import com.nlefler.glucloser.dataSource.*
import com.nlefler.glucloser.models.*

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

        var beforeSugarTask: Task<BloodSugar?>? = Task.forResult(null)
        var beforeSugarParcelable = bolusEventParcelable?.bloodSugarParcelable;
        if (beforeSugarParcelable != null) {
            beforeSugarTask = bloodSugarFactory.bloodSugarFromParcelable(beforeSugarParcelable)
        }

        val foodTasks = ArrayList<Task<Food?>>()
        val foodList = RealmList<Food>()
        for (foodParcelable in this.foodParcelableList) {
            foodTasks.add(foodFactory.foodFromParcelable(foodParcelable).continueWithTask(Continuation<Food?, Task<Food?>> { task ->
                if (task.isFaulted) {
                    return@Continuation task
                }

                val food = task.result
                foodList.add(food)
                return@Continuation Task.forResult(food)
            }))
        }

        var bolusPatternTask: Task<BolusPattern?>? = Task.forResult(null)
        if (bolusEventParcelable?.bolusPatternParcelable != null) {
            bolusPatternTask = bolusPatternFactory.bolusPatternFromParcelable(bolusEventParcelable?.bolusPatternParcelable!!)
        }

        val allTasks = arrayListOf(beforeSugarTask, bolusPatternTask)
        allTasks.addAll(foodTasks)
        Task.whenAll(allTasks).continueWith { task ->
            if (task.isFaulted) {
                return@continueWith
            }

            when (this.bolusEventParcelable ?: null) {
                is MealParcelable -> {
                    var placeTask: Task<Place?>? = Task.forResult(null)
                    if (this.placeParcelable != null) {
                        placeTask = placeFactory.placeFromParcelable(this.placeParcelable!!)
                    }

                    val mealTask = mealFactory.mealFromParcelable(this.bolusEventParcelable as MealParcelable)

                    Task.whenAll(arrayListOf(mealTask, placeTask)).continueWithTask(Continuation<Void, Task<Meal?>> { task ->
                        if (task.isFaulted) {
                            return@Continuation Task.forError<Meal?>(task.error)
                        }

                        val meal = mealTask.result
                        val place = placeTask?.result

                        return@Continuation realmManager.executeTransaction(object: RealmManager.Tx {
                            override fun dependsOn(): List<RealmObject?> {
                                return listOf(meal, place)
                            }

                            override fun execute(dependsOn: List<RealmObject?>, realm: Realm): List<RealmObject?> {
                                meal?.foods = foodList

                                if (place != null) {
                                    meal?.place = place
                                }

                                meal?.beforeSugar = beforeSugarTask?.result
                                meal?.bolusPattern = bolusPatternTask?.result
                                return listOf(meal)
                            }

                        }).continueWithTask(Continuation<List<RealmObject?>, Task<Meal?>> {task ->
                            if (task.isFaulted) {
                                return@Continuation Task.forError(task.error)
                            }
                            return@Continuation Task.forResult(task.result.firstOrNull() as Meal?)
                        })

                    }).continueWith { task ->
                        if (task.isFaulted || task.result == null || task.result !is Meal) {
                            return@continueWith
                        }

                        parseUploader.uploadBolusEvent(task.result as Meal)
                    }

                }
                is SnackParcelable -> {
                    snackFactory.snackFromParcelable(this.bolusEventParcelable as SnackParcelable).continueWithTask(Continuation<Snack?, Task<Snack?>> { task ->
                        if (task.isFaulted) {
                            return@Continuation task
                        }

                        val snack = task.result
                        return@Continuation realmManager.executeTransaction(object: RealmManager.Tx {
                            override fun dependsOn(): List<RealmObject?> {
                                return listOf(snack)
                            }

                            override fun execute(dependsOn: List<RealmObject?>, realm: Realm): List<RealmObject?> {
                                snack?.foods = foodList
                                snack?.beforeSugar = beforeSugarTask?.result
                                snack?.bolusPattern = bolusPatternTask?.result
                                return listOf(snack)
                            }
                        }).continueWithTask(Continuation<List<RealmObject?>, Task<Snack?>> { task ->
                            if (task.isFaulted) {
                                return@Continuation Task.forError(task.error)
                            }
                            return@Continuation Task.forResult(task.result.firstOrNull() as Snack?)
                        })
                    }).continueWith { task ->
                        if (task.isFaulted || task.result == null || task.result !is Snack) {
                            return@continueWith
                        }

                        parseUploader.uploadBolusEvent(task.result as Snack)
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
