package com.nlefler.glucloser.a.dataSource

import com.nlefler.glucloser.a.dataSource.jsonAdapter.FoodJsonAdapter
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.models.Food
import com.nlefler.glucloser.a.models.FoodEntity
import com.nlefler.glucloser.a.models.parcelable.FoodParcelable
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.requery.kotlin.eq
import io.requery.query.Result
import rx.Observable
import java.util.UUID
import javax.inject.Inject


/**
 * Created by Nathan Lefler on 5/19/15.
 */
public class FoodFactory @Inject constructor(val dbManager: DBManager) {
    private val LOG_TAG = "BloodSugarFactory"

    fun jsonAdapter(): JsonAdapter<FoodEntity> {
        return Moshi.Builder()
                .add(FoodJsonAdapter())
                .build()
                .adapter(FoodEntity::class.java)
    }

    fun foodFromParcelable(parcelable: FoodParcelable): Food {
        val f = FoodEntity()
        f.primaryID = parcelable.foodId
        f.carbs = parcelable.carbs ?: 0
        f.foodName = parcelable.foodName
        return f
    }

    public fun parcelableFromFood(food: Food): FoodParcelable {
        val parcelable = FoodParcelable()
        parcelable.foodId = food.primaryID
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

    private fun foodForFoodId(id: String): Observable<Result<FoodEntity>> {
        if (id.isEmpty()) {
            return Observable.error(Exception("Invalid Id"))
        }
        return dbManager.data.select(FoodEntity::class).where(Food::primaryID.eq(id)).get().toSelfObservable()
    }
}
