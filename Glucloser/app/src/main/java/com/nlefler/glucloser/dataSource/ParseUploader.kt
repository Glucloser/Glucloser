package com.nlefler.glucloser.dataSource

import android.util.Log
import com.nlefler.glucloser.models.*

import com.parse.ParseException
import com.parse.ParseObject
import com.parse.SaveCallback

import rx.Observable
import rx.Observer
import rx.Subscriber
import rx.functions.Action1
import rx.functions.Action2
import rx.schedulers.Schedulers
import java.util.*

/**
 * Created by Nathan Lefler on 12/30/14.
 */
public class ParseUploader private constructor() {
    private val inProgressUploads: MutableMap<String, Observable<ParseObject>>

    init {
        inProgressUploads = HashMap<String, Observable<ParseObject>>()
    }

    public fun uploadPlace(place: Place) {
        val placeId = place.foursquareId!!
        this.getUploadedObjectObservable(placeId, place).subscribe({ parseObject: ParseObject ->
            parseObject.saveInBackground()
            inProgressUploads.remove(placeId)
        })
    }

    public fun uploadBolusEvent(bolusEvent: BolusEvent) {

        var finalObservable: Observable<ParseObject>? = null
        var placeId: String = ""
        if (bolusEvent is HasPlace && bolusEvent.place != null) {
            val place = bolusEvent.place!!
            placeId = place.foursquareId!!
            finalObservable = getUploadedObjectObservable(placeId, place)
        }

        val beforeSugar = bolusEvent.beforeSugar
        val beforeSugarId: String = beforeSugar?.id ?: ""
        if (!beforeSugarId.isEmpty()) {
            val bolusObservable = getUploadedObjectObservable(beforeSugarId, beforeSugar!!)
            if (finalObservable != null) {
                finalObservable = Observable.merge<ParseObject>(finalObservable, bolusObservable)
            }
            else {
                finalObservable = bolusObservable
            }
        }

        val foodIds = ArrayList<String>()
        for (food in bolusEvent.foods) {
            val foodId = food.foodId
            foodIds.add(foodId)
            finalObservable = Observable.merge<ParseObject>(finalObservable, getUploadedObjectObservable(foodId, food))
        }

        if (finalObservable == null) {
            Log.e(LOG_TAG, "Can't upload bolus event, final observable is null")
            return
        }

        finalObservable.subscribe(object : Observer<ParseObject> {
            var placeParseObject: ParseObject? = null
            var beforeSugarParseObject: ParseObject? = null
            val foodParseObjects = ArrayList<ParseObject>()

            override fun onCompleted() {
                val bolusEventId = bolusEvent.id;
                getUploadedObjectObservable(bolusEventId, bolusEvent, beforeSugarParseObject, foodParseObjects, placeParseObject).subscribe({ mealObject: ParseObject ->
                    mealObject.saveInBackground()
                    inProgressUploads.remove(bolusEventId)
                })
            }

            override fun onError(e: Throwable) {
                Log.e(LOG_TAG, "Unable to save bolus event to Parse: " + e.getMessage())
            }

            override fun onNext(parseObject: ParseObject) {
                when (parseObject.getClassName()) {
                    Place.ParseClassName -> {
                        placeParseObject = parseObject
                        inProgressUploads.remove(placeId)
                    }
                    BloodSugar.ParseClassName -> {
                        beforeSugarParseObject = parseObject
                        inProgressUploads.remove(beforeSugarId)
                    }
                    Food.ParseClassName -> {
                        foodParseObjects.add(parseObject)
                        foodIds.remove(parseObject.get(Food.FoodIdFieldName))
                    }
                }
            }
        })
    }

    public fun uploadBloodSugar(sugar: BloodSugar) {
        val sugarId = sugar.id!!
        this.getUploadedObjectObservable(sugarId, sugar).subscribe({ parseObject: ParseObject ->
            parseObject.saveInBackground()
            inProgressUploads.remove(sugarId)
        })
    }

    public fun uploadFood(food: Food) {
        val foodId = food.foodId
        this.getUploadedObjectObservable(foodId, food).subscribe({ parseObject: ParseObject ->
            parseObject.saveInBackground()
            inProgressUploads.remove(foodId)
        })
    }

    /** Helpers  */
    @Synchronized
    private fun getUploadedObjectObservable(localId: String, toUpload: Any, vararg args: Any?): Observable<ParseObject> {
        if (this.inProgressUploads.containsKey(localId)) {
            return this.inProgressUploads.get(localId)!!
        } else {
            val observable = Observable.create<ParseObject>(object : Observable.OnSubscribe<ParseObject> {
                override fun call(subscriber: Subscriber<in ParseObject>) {
                    when (toUpload) {
                        is Place -> {
                            PlaceFactory.ParseObjectFromPlace(toUpload, createParseObjectReadyAction(subscriber))
                        }
                        is Meal -> {
                            if (args.size() < 3) {
                                subscriber.onError(IllegalArgumentException("Invalid specific arguments for Meal"))
                                return
                            }
                            @Suppress("UNCHECKED_CAST")
                            MealFactory.ParseObjectFromMeal(toUpload,
                                    args[0] as ParseObject?,
                                    args[1] as List<ParseObject>,
                                    args[2] as ParseObject?,
                                    createParseObjectReadyAction(subscriber))
                        }
                        is BloodSugar -> {
                            BloodSugarFactory.ParseObjectFromBloodSugar(toUpload, createParseObjectReadyAction(subscriber))
                        }
                        is Snack -> {
                            if (args.size() < 2) {
                                subscriber.onError(IllegalArgumentException("Invalid specific arguments for Snack"))
                                return
                            }
                            @Suppress("UNCHECKED_CAST")
                            SnackFactory.ParseObjectFromSnack(toUpload,
                                    args[0] as ParseObject?,
                                    args[1] as List<ParseObject>,
                                    createParseObjectReadyAction(subscriber))
                        }
                        is Food -> {
                            FoodFactory.ParseObjectFromFood(toUpload, createParseObjectReadyAction(subscriber))
                        }
                        else -> {
                            subscriber.onError(IllegalArgumentException("Invalid type for upload object"))
                        }
                    }
                }
            })
            this.inProgressUploads.put(localId, observable)
            return observable
        }
    }

    private fun createParseObjectReadyAction(subscriber: Subscriber<in ParseObject>): Action2<ParseObject?, Boolean> {
        return object : Action2<ParseObject?, Boolean> {
            override fun call(parseObject: ParseObject?, created: Boolean?) {
                if (parseObject == null) {
                    subscriber.onError(RuntimeException("Unable to create ParseObject"))
                    return
                }

                if (created!!) {
                    parseObject.saveInBackground({e: ParseException? ->
                        if (e == null) {
                            subscriber.onNext(parseObject)
                            subscriber.onCompleted()
                        } else {
                            subscriber.onError(e)
                        }
                    })
                } else {
                    subscriber.onNext(parseObject)
                    subscriber.onCompleted()
                }
            }
        }
    }

    companion object {
        private val LOG_TAG = "ParseUploader"

        private var _sharedInstance: ParseUploader? = null

        @Synchronized
        public fun SharedInstance(): ParseUploader {
            if (_sharedInstance == null) {
                _sharedInstance = ParseUploader()
            }
            return _sharedInstance!!
        }
    }
}
