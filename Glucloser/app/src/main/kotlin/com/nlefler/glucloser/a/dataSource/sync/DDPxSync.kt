package com.nlefler.glucloser.a.dataSource.sync

import android.util.Log
import bolts.Task
import com.nlefler.ddpx.DDPx
import com.nlefler.ddpx.connection.DDPxConnection
import com.nlefler.ddpx.method.DDPxMethodResult
import com.nlefler.glucloser.a.dataSource.BolusPatternFactory
import com.nlefler.glucloser.a.dataSource.MealFactory
import com.nlefler.glucloser.a.dataSource.PlaceFactory
import com.nlefler.glucloser.a.dataSource.SnackFactory
import com.nlefler.glucloser.a.models.*
import com.nlefler.glucloser.a.models.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by nathan on 1/31/16.
 */
@Singleton
class DDPxSync @Inject constructor(val newDDPx: (() -> DDPx), val snackFactory: SnackFactory,
                                   val mealFactory: MealFactory, val placeFactory: PlaceFactory,
                                   val bolusPatternFactory: BolusPatternFactory) {

    lateinit var ddpx: DDPx
    init {
        setup()
    }
    private fun setup() {
        ddpx =  newDDPx()
        ddpx.connect().continueWith { task ->
            if (task.isFaulted) {
                Log.e(DDPxSync.Companion.LOG_TAG, task.error.message)
                return@continueWith;
            }
            setupSubs()
        }
        // TODO(nl) Backoff reconnect attempts
        ddpx.connectionState().subscribe { state ->
            if (state == DDPxConnection.DDPxConnectionState.Disconnected) {
                setup()
            }
        }
    }

    fun call(method: String, args: Array<String>): Task<DDPxMethodResult> {
        return ddpx.method(method, args, null)
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
            Log.e(DDPxSync.Companion.LOG_TAG, e.message)
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
                Log.v(DDPxSync.Companion.LOG_TAG, "${change.collection} - ${change.type.name} - ${change.id}")

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
