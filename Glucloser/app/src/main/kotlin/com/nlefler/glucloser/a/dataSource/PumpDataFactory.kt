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
public class PumpDataFactory @Inject constructor(val pumpService: CairoPumpService) {

    private val LOG_TAG = "PumpDataFactory"

    fun currentCarbRatios(uuid: String): Observable<BolusPattern> {
        return pumpService.currentBolusPattern()
    }

    fun sensorReadingsAfter(date: Date): Observable<SensorReading> {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.HOUR, 2)
        val endDate = cal.time

        return pumpService.cgmReadingsBetween(date, endDate)
    }
}
