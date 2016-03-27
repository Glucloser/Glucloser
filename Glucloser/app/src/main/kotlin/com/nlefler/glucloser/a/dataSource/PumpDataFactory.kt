package com.nlefler.glucloser.a.dataSource

import android.util.Log
import bolts.Task
import com.nlefler.glucloser.a.dataSource.jsonAdapter.EJsonAdapter
import com.nlefler.glucloser.a.dataSource.sync.DDPxSync
import com.nlefler.glucloser.a.models.BolusPattern
import com.nlefler.glucloser.a.models.SensorReading
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.util.*
import javax.inject.Inject

/**
 * Created by nathan on 3/26/16.
 */
public class PumpDataFactory @Inject constructor(val realmManager: RealmManager,
                                                 val ddpxSync: DDPxSync,
                                                 val bloodSugarFactory: BloodSugarFactory,
                                                 val bolusPatternFactory: BolusPatternFactory,
                                                 val foodFactory: FoodFactory) {

    private val LOG_TAG = "PumpDataFactory"

    fun currentCarbRatios(uuid: String): Task<BolusPattern?> {
        return ddpxSync.call("currentCarbRatios", arrayOf(uuid)).continueWithTask { task ->
            if (task.isFaulted) {
                val error = Exception(task.error.message)
                return@continueWithTask Task.forError<BolusPattern?>(error)
            }
            try {
                val pattern = bolusPatternFactory.jsonAdapter().fromJson(task.result.result)
                return@continueWithTask Task.forResult(pattern)
            } catch (e: Exception) {
                Log.e(LOG_TAG, e.message)
                return@continueWithTask Task.forError<BolusPattern?>(e)
            }
        }
    }

    fun sensorReadingsAfter(date: Date): Task<Array<SensorReading>> {
        val ejson = ejsonAdapter()
        return ddpxSync.call("sensorReadingsAfter", arrayOf(ejson.toJson(date))).continueWithTask { task ->
            if (task.isFaulted) {
                return@continueWithTask Task.forError<Array<SensorReading>>(task.error)
            }

            try {
                val readingsAdapter = Moshi.Builder().add(EJsonAdapter()).build().adapter(Array<SensorReading>::class.java)
                val readings = readingsAdapter.fromJson(task.result.result)
                return@continueWithTask Task.forResult(readings)
            } catch (e: Exception) {
                return@continueWithTask Task.forError<Array<SensorReading>>(e)
            }
        }
    }

    private fun ejsonAdapter(): JsonAdapter<Date> {
        return Moshi.Builder()
                .add(EJsonAdapter())
                .build()
                .adapter(Date::class.java)
    }
}
