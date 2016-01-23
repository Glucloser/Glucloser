package com.nlefler.glucloser.dataSource

import android.content.Context
import android.util.Log
import bolts.Continuation
import bolts.Task
import bolts.TaskCompletionSource
import com.nlefler.glucloser.models.Food
import com.nlefler.glucloser.models.FoodParcelable
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import io.realm.Realm
import io.realm.RealmObject
import rx.functions.Action2
import java.util.UUID
import javax.inject.Inject


/**
 * Created by Nathan Lefler on 5/19/15.
 */
public class FoodFactory @Inject constructor(val realmManager: RealmManager) {
    private val LOG_TAG = "BloodSugarFactory"

    public fun food(): Task<Food?> {
        return foodForFoodId(UUID.randomUUID().toString(), true)
    }

    public fun foodFromParcelable(parcelable: FoodParcelable): Task<Food?> {
        return foodForFoodId(parcelable.foodId, true)
                .continueWithTask(Continuation<Food?, Task<Food?>> { task ->
            if (task.isFaulted) {
                return@Continuation task
            }

            val food = task.result
            return@Continuation realmManager.executeTransaction(object: RealmManager.Tx {
                override fun dependsOn(): List<RealmObject?> {
                    return listOf(food)
                }

                override fun execute(dependsOn: List<RealmObject?>, realm: Realm): List<RealmObject?> {
                    food?.foodName = parcelable.foodName
                    food?.carbs = parcelable.carbs
                    return listOf(food)
                }
            }).continueWithTask(Continuation<List<RealmObject?>, Task<Food?>> { task ->
                if (task.isFaulted) {
                    return@Continuation Task.forError(task.error)
                }
                val food = task.result.firstOrNull() as Food?
                return@Continuation Task.forResult(food)
            })
        })

    }

    public fun parcelableFromFood(food: Food): FoodParcelable {
        val parcelable = FoodParcelable()
        parcelable.foodId = food.primaryId
        parcelable.foodName = food.foodName
        parcelable.carbs = food.carbs
        return parcelable
    }

    public fun areFoodsEqual(food1: Food?, food2: Food?): Boolean {
        if (food1 == null || food2 == null) {
            return false
        }

        val nameOK = food1.foodName.equals(food2.foodName)
        val carbsOK = food1.carbs == food2.carbs

        return nameOK && carbsOK
    }

    internal fun foodFromParseObject(parseObject: ParseObject?): Task<Food?> {
        if (parseObject == null) {
            val errorMessage = "Can't create Food from Parse object, null"
            Log.e(LOG_TAG, errorMessage)
            return Task.forError(Exception(errorMessage))
        }

        val foodId = parseObject.getString(Food.FoodIdFieldName)
        if (foodId.length == 0) {
            Log.e(LOG_TAG, "Can't create Food from Parse object, no id")
        }
        val nameValue = parseObject.getString(Food.FoodNameFieldName)
        val carbValue = parseObject.getInt(Food.CarbsFieldName)

        return foodForFoodId(foodId, true).continueWithTask { task ->
            if (task.isFaulted) {
                return@continueWithTask Task.forError<Food?>(task.error)
            }
            val food = task.result
            return@continueWithTask realmManager.executeTransaction(object: RealmManager.Tx {
                override fun dependsOn(): List<RealmObject?> {
                    return listOf(food)
                }

                override fun execute(dependsOn: List<RealmObject?>, realm: Realm): List<RealmObject?> {
                    food?.foodName = nameValue
                    if (carbValue >= 0) {
                        food?.carbs = carbValue
                    }
                    return listOf(food)
                }
            }).continueWithTask(Continuation<List<RealmObject?>, Task<Food?>> { task ->
                if (task.isFaulted) {
                    return@Continuation Task.forError(task.error)
                }
                return@Continuation Task.forResult(task.result.firstOrNull() as Food?)
            })
        }
    }

    internal fun parseObjectFromFood(food: Food, action: Action2<ParseObject?, Boolean>?) {
        if (action == null) {
            Log.e(LOG_TAG, "Unable to create Parse object from Food, action null")
            return
        }
        if (food.primaryId.isEmpty()) {
            Log.e(LOG_TAG, "Unable to create Parse object from Food, blood sugar null or no id")
            action.call(null, false)
            return
        }

        val parseQuery = ParseQuery.getQuery<ParseObject>(Food.ParseClassName)
        parseQuery.whereEqualTo(Food.FoodIdFieldName, food.primaryId)

        parseQuery.findInBackground({parseObjects: List<ParseObject>, e: ParseException? ->
            val parseObject: ParseObject
            var created = false
            if (parseObjects.isEmpty()) {
                parseObject = ParseObject(Food.ParseClassName)
                created = true
            } else {
                parseObject = parseObjects.get(0)
            }
            parseObject.put(Food.FoodIdFieldName, food.primaryId)
            parseObject.put(Food.FoodNameFieldName, food.foodName)
            parseObject.put(Food.CarbsFieldName, food.carbs)
            action.call(parseObject, created)
        })
    }

    private fun foodForFoodId(id: String, create: Boolean): Task<Food?> {
        return realmManager.executeTransaction(object: RealmManager.Tx {
            override fun dependsOn(): List<RealmObject?> {
                return emptyList()
            }

            override fun execute(dependsOn: List<RealmObject?>, realm: Realm): List<RealmObject?> {
                if (create && id.isEmpty()) {
                    val food = realm.createObject<Food>(Food::class.java)
                    food!!.primaryId = UUID.randomUUID().toString()
                    food.carbs = 0
                    food.foodName = ""
                    return listOf(food)
                }

                val query = realm.where<Food>(Food::class.java)

                query?.equalTo(Food.FoodIdFieldName, id)
                var food = query?.findFirst()

                if (food == null && create) {
                    food = realm.createObject<Food>(Food::class.java)
                    food!!.primaryId = id
                    food.carbs = 0
                    food.foodName = ""
                }

                return listOf(food)
            }
        }).continueWithTask(Continuation<List<RealmObject?>, Task<Food?>> { task ->
            if (task.isFaulted) {
                return@Continuation Task.forError(task.error)
            }
            return@Continuation Task.forResult(task.result.firstOrNull() as Food?)
        })
    }
}
