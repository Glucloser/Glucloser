package com.nlefler.glucloser.a.dataSource

import bolts.Continuation
import bolts.Task
import bolts.TaskCompletionSource
import com.nlefler.glucloser.a.dataSource.jsonAdapter.FoodJsonAdapter
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.db.SQLStmts
import com.nlefler.glucloser.a.models.Food
import com.nlefler.glucloser.a.models.parcelable.FoodParcelable
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.util.UUID
import javax.inject.Inject


/**
 * Created by Nathan Lefler on 5/19/15.
 */
public class FoodFactory @Inject constructor(val dbManager: DBManager) {
    private val LOG_TAG = "BloodSugarFactory"

    fun jsonAdapter(): JsonAdapter<Food> {
        return Moshi.Builder()
                .add(FoodJsonAdapter())
                .build()
                .adapter(Food::class.java)
    }

    fun foodFromParcelable(parcelable: FoodParcelable): Food {
        return Food(parcelable.foodId, parcelable.carbs, parcelable.foodName)
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

    private fun foodForFoodId(id: String): Task<Food> {
        if (id.isEmpty()) {
            return Task.forError<Food>(Exception("Invalid ID"))
        }

        val task = TaskCompletionSource<Food>()
        val query = SQLStmts.Food.ForID()
        dbManager.query(query, arrayOf(id), { cursor ->
            if (cursor == null) {
                task.setError(Exception("Unable to read db"))
                return@query
            }
            if (!cursor.moveToFirst()) {
                task.setError(Exception("No result for id and create not set"))
                return@query
            }
            task.setResult(Food(id, query.getCarbs(cursor), query.getFoodName(cursor)))
            cursor.close()
        })
        return task.task
    }
}
