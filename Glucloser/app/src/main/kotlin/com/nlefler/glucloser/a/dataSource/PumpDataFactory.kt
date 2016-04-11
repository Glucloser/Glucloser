package com.nlefler.glucloser.a.dataSource

import android.util.Log
import bolts.Task
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoPumpService
import com.nlefler.glucloser.a.models.BolusPattern
import com.nlefler.glucloser.a.models.SensorReading
import rx.Observable
import java.util.*
import javax.inject.Inject

/**
 * Created by nathan on 3/26/16.
 */
public class PumpDataFactory @Inject constructor(val realmManager: RealmManager,
                                                 val pumpService: CairoPumpService,
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

    fun sensorReadingsAfter(date: Date): Observable<SensorReading> {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.HOUR, 2)
        val endDate = cal.time

        return pumpService.cgmReadingsBetween(date, endDate)
    }
}
