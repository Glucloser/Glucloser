package com.nlefler.glucloser.dataSource

import android.content.Context
import android.util.Log
import com.nlefler.glucloser.models.Food
import com.nlefler.glucloser.models.FoodParcelable
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import io.realm.Realm
import rx.functions.Action2
import java.util.UUID
import javax.inject.Inject


/**
 * Created by Nathan Lefler on 5/19/15.
 */
public class FoodFactory @Inject constructor(val realm: Realm) {
    private val LOG_TAG = "BloodSugarFactory"

    public fun food(): Food {
        realm.beginTransaction()
        val food = foodForFoodId("", true)!!
        realm.commitTransaction()

        return food
    }

    public fun foodFromParcelable(parcelable: FoodParcelable): Food {
        realm.beginTransaction()
        val food = foodForFoodId(parcelable.foodId, true)!!
        food.name = parcelable.foodName
        food.carbs = parcelable.carbs
        realm.commitTransaction()

        return food
    }

    public fun parcelableFromFood(food: Food): FoodParcelable {
        val parcelable = FoodParcelable()
        parcelable.foodId = food.foodId
        parcelable.foodName = food.name
        parcelable.carbs = food.carbs
        return parcelable
    }

    public fun areFoodsEqual(food1: Food?, food2: Food?): Boolean {
        if (food1 == null || food2 == null) {
            return false
        }

        val nameOK = food1.name.equals(food2.name)
        val carbsOK = food1.carbs == food2.carbs

        return nameOK && carbsOK
    }

    internal fun foodFromParseObject(parseObject: ParseObject?): Food? {
        if (parseObject == null) {
            Log.e(LOG_TAG, "Can't create Food from Parse object, null")
            return null
        }
        val foodId = parseObject.getString(Food.FoodIdFieldName)
        if (foodId.length() == 0) {
            Log.e(LOG_TAG, "Can't create Food from Parse object, no id")
        }
        val nameValue = parseObject.getString(Food.FoodNameFieldName)
        val carbValue = parseObject.getInt(Food.CarbsFieldName)

        realm.beginTransaction()
        val food = foodForFoodId(foodId, true)!!
        food.name = nameValue
        if (carbValue >= 0) {
            food.carbs = carbValue
        }
        realm.commitTransaction()

        return food
    }

    internal fun parseObjectFromFood(food: Food, action: Action2<ParseObject?, Boolean>?) {
        if (action == null) {
            Log.e(LOG_TAG, "Unable to create Parse object from Food, action null")
            return
        }
        if (food.foodId.isEmpty()) {
            Log.e(LOG_TAG, "Unable to create Parse object from Food, blood sugar null or no id")
            action.call(null, false)
            return
        }

        val parseQuery = ParseQuery.getQuery<ParseObject>(Food.ParseClassName)
        parseQuery.whereEqualTo(Food.FoodIdFieldName, food.foodId)

        parseQuery.findInBackground({parseObjects: List<ParseObject>, e: ParseException? ->
            val parseObject: ParseObject
            var created = false
            if (parseObjects.isEmpty()) {
                parseObject = ParseObject(Food.ParseClassName)
                created = true
            } else {
                parseObject = parseObjects.get(0)
            }
            parseObject.put(Food.FoodIdFieldName, food.foodId)
            parseObject.put(Food.FoodNameFieldName, food.name)
            parseObject.put(Food.CarbsFieldName, food.carbs)
            action.call(parseObject, created)
        })
    }

    private fun foodForFoodId(id: String, create: Boolean): Food? {
        if (create && id.isEmpty()) {
            val food = realm.createObject<Food>(Food::class.java)
            return food
        }

        val query = realm.where<Food>(Food::class.java)

        query?.equalTo(Food.FoodIdFieldName, id)
        var result: Food? = query?.findFirst()

        if (result == null && create) {
            result = realm.createObject<Food>(Food::class.java)
            result!!.foodId = id
        }

        return result
    }
}
