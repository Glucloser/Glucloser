package com.nlefler.glucloser.dataSource

import android.content.Context
import android.util.Log
import com.nlefler.glucloser.models.BloodSugar
import com.nlefler.glucloser.models.Snack
import com.nlefler.glucloser.models.SnackParcelable

import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery

import java.util.UUID

import io.realm.Realm
import rx.functions.Action1
import rx.functions.Action2

/**
 * Created by Nathan Lefler on 4/25/15.
 */
public class SnackFactory {

    companion object {
        private val LOG_TAG = "SnackFactory"

        public fun Snack(ctx: Context): Snack {
            val realm = Realm.getInstance(ctx)

            realm.beginTransaction()
            val snack = SnackForSnackId("", realm, true)!!
            realm.commitTransaction()

            return snack
        }

        public fun FetchSnack(id: String, ctx: Context?, action: Action1<Snack>?) {
            if (action == null) {
                Log.e(LOG_TAG, "Unable to fetch Snack, action is null")
                return
            }
            if (id == null || id.isEmpty() || ctx == null) {
                Log.e(LOG_TAG, "Unable to fetch Snack, invalid args")
                action.call(null)
                return
            }
            val realm = Realm.getInstance(ctx)
            realm.beginTransaction()
            val snack = SnackForSnackId(id, realm, false)
            if (snack != null) {
                action.call(snack)
                return
            }

            val parseQuery = ParseQuery.getQuery<ParseObject>(Snack.ParseClassName)
            parseQuery.whereEqualTo(Snack.SnackIdFieldName, id)
            parseQuery.setLimit(1)
            parseQuery.firstInBackground.continueWithTask({ task ->
                if (task.isFaulted) {
                    action.call(null)
                    task
                }
                else {
                    task.result.fetchIfNeededInBackground<ParseObject>()
                }
            }).continueWith({ task ->
                if (task.isFaulted) {
                    action.call(null)
                } else {
                    val snackFromParse = SnackFromParseObject(task.result, realm)
                    action.call(snackFromParse)
                }
            })
        }

        public fun ParcelableFromSnack(snack: Snack): SnackParcelable {
            val parcelable = SnackParcelable()
            parcelable.carbs = snack.carbs
            parcelable.insulin = snack.insulin
            parcelable.id = snack.id
            parcelable.isCorrection = snack.isCorrection
            if (snack.beforeSugar != null) {
                parcelable.beforeSugarParcelable = BloodSugarFactory.ParcelableFromBloodSugar(snack.beforeSugar!!)
            }
            parcelable.date = snack.date

            return parcelable
        }

        public fun SnackFromParcelable(parcelable: SnackParcelable, ctx: Context): Snack {
            val realm = Realm.getInstance(ctx)

            var beforeSugar: BloodSugar? = null
            if (parcelable.beforeSugarParcelable != null) {
                beforeSugar = BloodSugarFactory.BloodSugarFromParcelable(parcelable.beforeSugarParcelable!!, ctx)
            }

            realm.beginTransaction()
            val snack = SnackForSnackId(parcelable.id, realm, true)!!
            snack.insulin = parcelable.insulin
            snack.carbs = parcelable.carbs
            snack.isCorrection = parcelable.isCorrection
            if (beforeSugar != null) {
                snack.beforeSugar = beforeSugar
            }
            snack.date = parcelable.date
            realm.commitTransaction()

            return snack
        }

        protected fun SnackFromParseObject(parseObject: ParseObject?, realm: Realm?): Snack? {
            if (parseObject == null || realm == null) {
                Log.e(LOG_TAG, "Can't create Snack from Parse object, null")
                return null
            }
            val snackId = parseObject.getString(Snack.SnackIdFieldName)
            if (snackId == null || snackId.isEmpty()) {
                Log.e(LOG_TAG, "Can't create Snack from Parse object, no id")
            }
            val carbs = parseObject.getInt(Snack.CarbsFieldName)
            val insulin = parseObject.getDouble(Snack.InsulinFieldName).toFloat()
            val correction = parseObject.getBoolean(Snack.CorrectionFieldName)
            val snackDate = parseObject.getDate(Snack.SnackDateFieldName)
            val beforeSugar = BloodSugarFactory.BloodSugarFromParseObject(parseObject.getParseObject(Snack.BeforeSugarFieldName), realm)

            realm.beginTransaction()
            val snack = SnackForSnackId(snackId, realm, true)!!
            if (carbs >= 0 && carbs != snack.carbs) {
                snack.carbs = carbs
            }
            if (insulin >= 0 && snack.insulin != insulin) {
                snack.insulin = insulin
            }
            if (beforeSugar != null && !BloodSugarFactory.AreBloodSugarsEqual(snack.beforeSugar, beforeSugar)) {
                snack.beforeSugar = beforeSugar
            }
            if (snack.isCorrection != correction) {
                snack.isCorrection = correction
            }
            if (snackDate != null) {
                snack.date = snackDate
            }
            realm.commitTransaction()

            return snack
        }

        /**
         * Fetches or creates a ParseObject representing the provided Snack
         * @param snack
         * *
         * @param action Returns the ParseObject, and true if the object was created and should be saved.
         */
        internal fun ParseObjectFromSnack(snack: Snack?,
                                          beforeSugarObject: ParseObject?,
                                          foodObjects: List<ParseObject>,
                                          action: Action2<ParseObject?, Boolean>?) {
            if (action == null) {
                Log.e(LOG_TAG, "Unable to create Parse object from Snack, action null")
                return
            }
            if (snack?.id?.isEmpty() ?: true) {
                Log.e(LOG_TAG, "Unable to create Parse object from Snack, Snack null or no id")
                action.call(null, false)
                return
            }

            val parseQuery = ParseQuery.getQuery<ParseObject>(Snack.ParseClassName)
            parseQuery.whereEqualTo(Snack.SnackIdFieldName, snack!!.id)
            parseQuery.findInBackground({parseObjects: List<ParseObject>, e: ParseException? ->
                val parseObject: ParseObject
                var created = false
                if (parseObjects.isEmpty()) {
                    parseObject = ParseObject(Snack.ParseClassName)
                    created = true
                } else {
                    parseObject = parseObjects.get(0)
                }
                parseObject.put(Snack.SnackIdFieldName, snack.id)
                if (beforeSugarObject != null) {
                    parseObject.put(Snack.BeforeSugarFieldName, beforeSugarObject)
                }
                parseObject.put(Snack.CorrectionFieldName, snack.isCorrection)
                parseObject.put(Snack.CarbsFieldName, snack.carbs)
                parseObject.put(Snack.InsulinFieldName, snack.insulin)
                parseObject.put(Snack.SnackDateFieldName, snack.date)
                parseObject.put(Snack.FoodListFieldName, foodObjects)
                action.call(parseObject, created)
            })
        }

        private fun SnackForSnackId(id: String, realm: Realm, create: Boolean): Snack? {
            if (create && id.isEmpty()) {
                val snack = realm.createObject<Snack>(Snack::class.java)
                snack.id = UUID.randomUUID().toString()
                return snack
            }

            val query = realm.where<Snack>(javaClass<Snack>())

            query.equalTo(Snack.SnackIdFieldName, id)
            var result: Snack? = query.findFirst()

            if (result == null && create) {
                result = realm.createObject<Snack>(javaClass<Snack>())
                result!!.id = id
            }

            return result
        }
    }
}
