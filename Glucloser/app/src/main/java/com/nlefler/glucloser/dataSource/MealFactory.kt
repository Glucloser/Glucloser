package com.nlefler.glucloser.dataSource

import android.content.Context
import android.util.Log
import bolts.Task
import com.nlefler.glucloser.models.BloodSugar
import com.nlefler.glucloser.models.Meal
import com.nlefler.glucloser.models.MealParcelable
import com.nlefler.glucloser.models.Place

import com.parse.FindCallback
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery

import java.util.Date
import java.util.UUID

import io.realm.Realm
import io.realm.RealmQuery
import rx.functions.Action1
import rx.functions.Action2

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class MealFactory {

    companion object {
        private val LOG_TAG = "MealFactory"

        public fun Meal(ctx: Context): Meal {
            val realm = Realm.getInstance(ctx)

            realm.beginTransaction()
            val meal = MealForMealId("", realm, true)!!
            realm.commitTransaction()

            return meal
        }

        public fun FetchMeal(id: String, ctx: Context?, action: Action1<Meal>?) {
            if (action == null) {
                Log.e(LOG_TAG, "Unable to fetch Meal, action is null")
                return
            }
            if (id.length() == 0|| ctx == null) {
                Log.e(LOG_TAG, "Unable to fetch Meal, invalid args")
                action.call(null)
                return
            }
            val realm = Realm.getInstance(ctx)
            realm.beginTransaction()
            val meal = MealForMealId(id, realm, false)
            if (meal != null) {
                action.call(meal)
                return
            }

            val parseQuery = ParseQuery.getQuery<ParseObject>(Meal.ParseClassName)
            parseQuery.whereEqualTo(Meal.MealIdFieldName, id)
            parseQuery.findInBackground({parseObjects: List<ParseObject>, e: ParseException ->
                if (!parseObjects.isEmpty()) {
                    val mealFromParse = MealFromParseObject(parseObjects.get(0), realm)
                    action.call(mealFromParse)
                } else {
                    action.call(null)
                }
            })
        }

        public fun ParcelableFromMeal(meal: Meal): MealParcelable {
            val parcelable = MealParcelable()
            if (meal.place != null) {
                parcelable.placeParcelable = PlaceFactory.ParcelableFromPlace(meal.place!!)
            }
            parcelable.carbs = meal.carbs
            parcelable.insulin = meal.insulin
            parcelable.id = meal.id
            parcelable.isCorrection = meal.isCorrection
            if (meal.beforeSugar != null) {
                parcelable.bloodSugarParcelable = BloodSugarFactory.ParcelableFromBloodSugar(meal.beforeSugar!!)
            }
            parcelable.date = meal.date
            if (meal.bolusPattern != null) {
                parcelable.bolusPatternParcelable = BolusPatternFactory.ParcelableFromBolusPattern(meal.bolusPattern!!)
            }

            return parcelable
        }

        public fun MealFromParcelable(parcelable: MealParcelable, ctx: Context): Meal {
            val realm = Realm.getInstance(ctx)

            var place: Place? = null
            if (parcelable.placeParcelable != null) {
                place = PlaceFactory.PlaceFromParcelable(parcelable.placeParcelable!!, ctx)
            }

            var beforeSugar: BloodSugar? = null
            if (parcelable.bloodSugarParcelable != null) {
                beforeSugar = BloodSugarFactory.BloodSugarFromParcelable(parcelable.bloodSugarParcelable!!, ctx)
            }

            realm.beginTransaction()
            val meal = MealForMealId(parcelable.id, realm, true)!!
            meal.insulin = parcelable.insulin
            meal.carbs = parcelable.carbs
            meal.place = place
            meal.beforeSugar = beforeSugar
            meal.isCorrection = parcelable.isCorrection
            meal.date = parcelable.date
            if (parcelable.bolusPatternParcelable != null) {
                meal.bolusPattern = BolusPatternFactory.BolusPatternFromParcelable(parcelable.bolusPatternParcelable!!)
            }
            realm.commitTransaction()

            return meal
        }

        protected fun MealFromParseObject(parseObject: ParseObject?, realm: Realm?): Meal? {
            if (parseObject == null || realm == null) {
                Log.e(LOG_TAG, "Can't create Meal from Parse object, null")
                return null
            }
            val mealId = parseObject.getString(Meal.MealIdFieldName)
            if (mealId?.length() == 0) {
                Log.e(LOG_TAG, "Can't create Meal from Parse object, no id")
            }
            val carbs = parseObject.getInt(Meal.CarbsFieldName)
            val insulin = parseObject.getDouble(Meal.InsulinFieldName).toFloat()
            val correction = parseObject.getBoolean(Meal.CorrectionFieldName)
            val mealDate = parseObject.getDate(Meal.MealDateFieldName)
            val place = PlaceFactory.PlaceFromParseObject(parseObject.getParseObject(Meal.PlaceFieldName), realm)
            val beforeSugar = BloodSugarFactory.BloodSugarFromParseObject(parseObject.getParseObject(Meal.BeforeSugarFieldName), realm)

            realm.beginTransaction()
            val meal = MealForMealId(mealId, realm, true)!!
            if (carbs >= 0 && carbs != meal.carbs) {
                meal.carbs = carbs
            }
            if (insulin >= 0 && meal.insulin != insulin) {
                meal.insulin = insulin
            }
            if (beforeSugar != null && !BloodSugarFactory.AreBloodSugarsEqual(meal.beforeSugar, beforeSugar)) {
                meal.beforeSugar = beforeSugar
            }
            if (meal.isCorrection != correction) {
                meal.isCorrection = correction
            }
            if (place != null && !PlaceFactory.ArePlacesEqual(place, meal.place)) {
                meal.place = place
            }
            if (mealDate != null) {
                meal.date = mealDate
            }
            meal.bolusPattern = BolusPatternFactory.BolusPatternFromParseObject(parseObject.getParseObject(Meal.BolusPatternFieldName))
            realm.commitTransaction()

            return meal
        }

        /**
         * Fetches or creates a ParseObject representing the provided Meal
         * @param meal
         * *
         * @param action Returns the ParseObject, and true if the object was created and should be saved.
         */
        internal fun ParseObjectFromMeal(meal: Meal,
                                         beforeSugarObject: ParseObject?,
                                         foodObjects: List<ParseObject>,
                                         placeObject: ParseObject?,
                                         action: Action2<ParseObject?, Boolean>?) {
            if (action == null) {
                Log.e(LOG_TAG, "Unable to create Parse object from Meal, action null")
                return
            }
            if (meal.id.length() == 0) {
                Log.e(LOG_TAG, "Unable to create Parse object from Meal, meal null or no id")
                action.call(null, false)
                return
            }

            val parseQuery = ParseQuery.getQuery<ParseObject>(Meal.ParseClassName)
            parseQuery.whereEqualTo(Meal.MealIdFieldName, meal.id)
            parseQuery.setLimit(1)
            parseQuery.firstInBackground.continueWithTask({ task ->
                if (task.result == null) {
                    Task.forResult(Pair(ParseObject(Meal.ParseClassName), true))
                } else {
                    Task.forResult(Pair(task.result, false))
                }
            }).continueWith({ task ->
                val resultPair = task.result
                val parseObject = resultPair.first
                val created = resultPair.second
                parseObject.put(Meal.MealIdFieldName, meal.id)
                if (placeObject != null) {
                    parseObject.put(Meal.PlaceFieldName, placeObject)
                }
                if (beforeSugarObject != null) {
                    parseObject.put(Meal.BeforeSugarFieldName, beforeSugarObject)
                }
                parseObject.put(Meal.CorrectionFieldName, meal.isCorrection)
                parseObject.put(Meal.CarbsFieldName, meal.carbs)
                parseObject.put(Meal.InsulinFieldName, meal.insulin)
                parseObject.put(Meal.MealDateFieldName, meal.date)
                parseObject.put(Meal.FoodListFieldName, foodObjects)
                if (meal.bolusPattern != null) {
                    parseObject.put(Meal.BolusPatternFieldName, BolusPatternFactory.ParseObjectFromBolusPattern(meal.bolusPattern!!))
                }
                action.call(parseObject, created)
            })
        }

        private fun MealForMealId(id: String, realm: Realm, create: Boolean): Meal? {
            if (create && id.length() == 0) {
                val meal = realm.createObject<Meal>(Meal::class.java)
                meal.id = UUID.randomUUID().toString()
                return meal
            }

            val query = realm.where<Meal>(Meal::class.java)

            query.equalTo(Meal.MealIdFieldName, id)
            var result: Meal? = query.findFirst()

            if (result == null && create) {
                result = realm.createObject<Meal>(Meal::class.java)
                result!!.id = id
            }

            return result
        }
    }
}
