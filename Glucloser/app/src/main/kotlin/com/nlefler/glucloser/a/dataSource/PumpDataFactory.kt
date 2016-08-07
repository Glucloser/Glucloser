package com.nlefler.glucloser.a.dataSource

import android.util.Log
import com.nlefler.glucloser.a.dataSource.sync.cairo.CairoServices
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoPumpService
import com.nlefler.glucloser.a.models.BolusPattern
import com.nlefler.glucloser.a.models.BolusPatternEntity
import com.nlefler.glucloser.a.models.SensorReading
import rx.Observable
import java.util.*
import javax.inject.Inject

/**
 * Created by nathan on 3/26/16.
 */
public class PumpDataFactory @Inject constructor(userServices: CairoServices) {

    private val LOG_TAG = "PumpDataFactory"
    private val pumpService = userServices.pumpService()

    fun currentCarbRatios(uuid: String): Observable<BolusPatternEntity> {
        return pumpService.currentBolusPattern()
    }

    fun sensorReadingsAfter(date: Date): Observable<SensorReading> {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.HOUR, 2)
        val endDate = cal.time

        return pumpService.cgmReadingsBetween(CairoPumpService.CGMReadingsBetweenBody(date, endDate))
    }
}
