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
            val food = FoodForFoodId(null, realm, true)!!
            realm.commitTransaction()

            return food
        }

        public fun FoodFromParcelable(parcelable: FoodParcelable, ctx: Context): Food {
            val realm = Realm.getInstance(ctx)

            realm.beginTransaction()
            val food = FoodForFoodId(parcelable.getFoodId(), realm, true)!!
            food.setName(parcelable.getFoodName())
            food.setCarbs(parcelable.getCarbs())
            realm.commitTransaction()

            return food
        }

        public fun ParcelableFromFood(food: Food): FoodParcelable {
            val parcelable = FoodParcelable()
            parcelable.setFoodId(food.getFoodId())
            parcelable.setFoodName(food.getName())
            parcelable.setCarbs(food.getCarbs())
            return parcelable
        }

        public fun AreFoodsEqual(food1: Food?, food2: Food?): Boolean {
            if (food1 == null || food2 == null) {
                return false
            }

            val nameOK = food1.getName().equals(food2.getName())
            val carbsOK = food1.getCarbs() == food2.getCarbs()

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
            food.setName(nameValue)
            if (carbValue >= 0) {
                food.setCarbs(carbValue)
            }
            realm.commitTransaction()

            return food
        }

        internal fun ParseObjectFromFood(food: Food, action: Action2<ParseObject?, Boolean>?) {
            if (action == null) {
                Log.e(LOG_TAG, "Unable to create Parse object from Food, action null")
                return
            }
            if (food.getFoodId()?.isEmpty() ?: true) {
                Log.e(LOG_TAG, "Unable to create Parse object from Food, blood sugar null or no id")
                action.call(null, false)
                return
            }

            val parseQuery = ParseQuery.getQuery<ParseObject>(Food.ParseClassName)
            parseQuery.whereEqualTo(Food.FoodIdFieldName, food.getFoodId())

            parseQuery.findInBackground({parseObjects: List<ParseObject>, e: ParseException? ->
                val parseObject: ParseObject
                var created = false
                if (parseObjects.isEmpty()) {
                    parseObject = ParseObject(Food.ParseClassName)
                    created = true
                } else {
                    parseObject = parseObjects.get(0)
                }
                parseObject.put(Food.FoodIdFieldName, food.getFoodId())
                parseObject.put(Food.FoodNameFieldName, food.getName())
                parseObject.put(Food.CarbsFieldName, food.getCarbs())
                action.call(parseObject, created)
            })
        }

        private fun FoodForFoodId(id: String?, realm: Realm, create: Boolean): Food? {
            if (create && (id == null || id.isEmpty())) {
                val food = realm.createObject<Food>(javaClass<Food>())
                food.setFoodId(UUID.randomUUID().toString())
                return food
            }

            val query = realm.where<Food>(javaClass<Food>())

            query.equalTo(Food.FoodIdFieldName, id)
            var result: Food? = query.findFirst()

            if (result == null && create) {
                result = realm.createObject<Food>(javaClass<Food>())
                result!!.setFoodId(id)
            }

            return result
        }
    }
}
