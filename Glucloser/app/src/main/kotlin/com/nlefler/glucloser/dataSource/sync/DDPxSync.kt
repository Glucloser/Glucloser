package com.nlefler.glucloser.dataSource.sync

import android.util.Log
import bolts.Task
import com.nlefler.ddpx.DDPx
import com.nlefler.glucloser.dataSource.BolusPatternFactory
import com.nlefler.glucloser.dataSource.MealFactory
import com.nlefler.glucloser.dataSource.PlaceFactory
import com.nlefler.glucloser.dataSource.SnackFactory
import com.nlefler.glucloser.models.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by nathan on 1/31/16.
 */
@Singleton
class DDPxSync @Inject constructor(val ddpx: DDPx, val snackFactory: SnackFactory,
                                   val mealFactory: MealFactory, val placeFactory: PlaceFactory,
                                   val bolusPatternFactory: BolusPatternFactory) {

    init {
        ddpx.connect().continueWith { task ->
            if (task.isFaulted) {
                Log.e(LOG_TAG, task.error.message)
                return@continueWith;
            }
            setupSubs()
        }
    }

    fun createUserOrLogin(email: String, uuid: String): Task<String?> {
        return ddpx.method("createOrLogin", arrayOf(email, uuid), null).continueWithTask { task ->
            if (task.isFaulted) {
                val error = Exception(task.error.message)
                return@continueWithTask Task.forError<String>(error)
            }
            return@continueWithTask Task.forResult(task.result.result)
        }
    }

    fun savePushToken(uuid: String, token: String): Task<String?> {
        return ddpx.method("savePushToken", arrayOf(uuid, token), null).continueWithTask { task ->
            if (task.isFaulted) {
                val error = Exception(task.error.message)
                return@continueWithTask Task.forError<String>(error)
            }
            return@continueWithTask Task.forResult(task.result.result)
        }
    }

    fun saveFoursquareId(uuid: String, fsqId: String): Task<String?> {
        return ddpx.method("saveFoursquareId", arrayOf(uuid, fsqId), null).continueWithTask { task ->
            if (task.isFaulted) {
                val error = Exception(task.error.message)
                return@continueWithTask Task.forError<String>(error)
            }
            return@continueWithTask Task.forResult(task.result.result)
        }
    }

    fun currentCarbRatios(uuid: String): Task<BolusPattern?> {
        return ddpx.method("currentCarbRatios", arrayOf(uuid), null).continueWithTask { task ->
            if (task.isFaulted) {
                val error = Exception(task.error.message)
                return@continueWithTask Task.forError<BolusPattern?>(error)
            }
            try {
                val pattern = bolusPatternFactory.jsonAdapter().fromJson(task.result.result)
                return@continueWithTask Task.forResult(pattern)
            } catch (e: Exception) {
                Log.e("", e.message)
                return@continueWithTask Task.forError<BolusPattern?>(e)
            }
        }
    }

    fun saveModel(model: Syncable): Task<Unit> {
        var methodName = "noop";
        var json = ""
        try {
            when (model) {
                is Snack -> {
                    methodName = "addSnack"
                    json = snackFactory.jsonAdapter().toJson(model)
                }
                is Meal -> {
                    methodName = "addMeal"
                    json = mealFactory.jsonAdapter().toJson(model)
                }
                is Place -> {
                    methodName = "addPlace"
                    json = placeFactory.jsonAdapter().toJson(model)
                }
                else -> Unit
            }
        }
        catch (e: Exception) {
            Log.e(LOG_TAG, e.message)
            return Task.forError(e)
        }

        return ddpx.method(methodName, arrayOf(json), null).continueWithTask { task ->
            if (task.isFaulted) {
                val error = Exception(task.error.message)
                return@continueWithTask Task.forError<Unit>(error)
            }
            return@continueWithTask Task.forResult(Unit)
        }
    }

    private fun setupSubs() {
        listOf(Place.ModelName, BloodSugar.ModelName, BolusPattern.ModelName,
                BolusRate.ModelName, Food.ModelName, Meal.ModelName, Snack.ModelName).forEach { modelName ->
            ddpx.sub(modelName, null).subscribe { change ->
                Log.v(LOG_TAG, "${change.collection} - ${change.type.name} - ${change.id}")

                // DDP returns modified fields only
                // So in event of 'added' or 'changed':
                // Find object for id
                // If there is none, create it and set fields (should have all fields in this case)
                // Else set fields from change event and save
                // TODO(nl) Handle change events from server
            }
        }
    }

    companion object {
        private val LOG_TAG = "DDPxSync"
    }
}
