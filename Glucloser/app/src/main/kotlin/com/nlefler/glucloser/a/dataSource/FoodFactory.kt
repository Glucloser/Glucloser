package com.nlefler.glucloser.a.dataSource

import android.content.Context
import android.util.Log
import bolts.Continuation
import bolts.Task
import bolts.TaskCompletionSource
import com.nlefler.glucloser.a.dataSource.jsonAdapter.EJsonAdapter
import com.nlefler.glucloser.a.dataSource.jsonAdapter.FoodJsonAdapter
import com.nlefler.glucloser.a.models.Food
import com.nlefler.glucloser.a.models.parcelable.FoodParcelable
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
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

    public fun jsonAdapter(): JsonAdapter<Food> {
        return Moshi.Builder()
                .add(FoodJsonAdapter(realmManager.defaultRealm()))
                .add(EJsonAdapter())
                .build()
                .adapter(Food::class.java)
    }

    public fun foodFromParcelable(parcelable: FoodParcelable): Task<Food?> {
        return foodForFoodId(parcelable.foodId, true)
                .continueWithTask(Continuation<Food?, Task<Food?>> foodForId@ { task ->
                    if (task.isFaulted) {
                        return@foodForId task
                    }

                    val food = task.result
                    return@foodForId realmManager.executeTransaction(object : RealmManager.Tx<Food?> {
                        override fun dependsOn(): List<RealmObject?> {
                            return listOf(food)
                        }

                        override fun execute(dependsOn: List<RealmObject?>, realm: Realm): Food? {
                            val liveFood = dependsOn.first() as Food?
                            liveFood?.foodName = parcelable.foodName
                            liveFood?.carbs = parcelable.carbs
                            return liveFood
                        }
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

    private fun foodForFoodId(id: String, create: Boolean): Task<Food?> {
        return realmManager.executeTransaction(object: RealmManager.Tx<Food?> {
            override fun dependsOn(): List<RealmObject?> {
                return emptyList()
            }

            override fun execute(dependsOn: List<RealmObject?>, realm: Realm): Food? {
                if (create && id.isEmpty()) {
                    val food = realm.createObject<Food>(Food::class.java)
                    food!!.primaryId = UUID.randomUUID().toString()
                    food.carbs = 0
                    food.foodName = ""
                    return food
                }

                val query = realm.where<Food>(Food::class.java)

                query?.equalTo(Food.IdFieldName, id)
                var food = query?.findFirst()

                if (food == null && create) {
                    food = realm.createObject<Food>(Food::class.java)
                    food!!.primaryId = id
                    food.carbs = 0
                    food.foodName = ""
                }

                return food
            }
        })
    }
}
