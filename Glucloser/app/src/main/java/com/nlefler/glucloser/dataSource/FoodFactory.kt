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


/**
 * Created by Nathan Lefler on 5/19/15.
 */
public class FoodFactory {
        companion object {
        private val LOG_TAG = "BloodSugarFactory"

        public fun Food(ctx: Context): Food {
            val realm = Realm.getInstance(ctx)

            realm.beginTransaction()
            val food = FoodForFoodId("", realm, true)!!
            realm.commitTransaction()

            return food
        }

        public fun FoodFromParcelable(parcelable: FoodParcelable, ctx: Context): Food {
            val realm = Realm.getInstance(ctx)

            realm.beginTransaction()
            val food = FoodForFoodId(parcelable.foodId, realm, true)!!
            food.name = parcelable.foodName
            food.carbs = parcelable.carbs
            realm.commitTransaction()

            return food
        }

        public fun ParcelableFromFood(food: Food): FoodParcelable {
            val parcelable = FoodParcelable()
            parcelable.foodId = food.foodId
            parcelable.foodName = food.name
            parcelable.carbs = food.carbs
            return parcelable
        }

        public fun AreFoodsEqual(food1: Food?, food2: Food?): Boolean {
            if (food1 == null || food2 == null) {
                return false
            }

            val nameOK = food1.name.equals(food2.name)
            val carbsOK = food1.carbs == food2.carbs

            return nameOK && carbsOK
        }

        internal fun FoodFromParseObject(parseObject: ParseObject?, realm: Realm?): Food? {
            if (parseObject == null || realm == null) {
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
            val food = FoodForFoodId(foodId, realm, true)!!
            food.name = nameValue
            if (carbValue >= 0) {
                food.carbs = carbValue
            }
            realm.commitTransaction()

            return food
        }

        internal fun ParseObjectFromFood(food: Food, action: Action2<ParseObject?, Boolean>?) {
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

        private fun FoodForFoodId(id: String, realm: Realm, create: Boolean): Food? {
            if (create && id.isEmpty()) {
                val food = realm.createObject<Food>(Food::class.java)
                return food
            }

            val query = realm.where<Food>(Food::class.java)

            query.equalTo(Food.FoodIdFieldName, id)
            var result: Food? = query.findFirst()

            if (result == null && create) {
                result = realm.createObject<Food>(javaClass<Food>())
                result!!.foodId = id
            }

            return result
        }
    }
}
