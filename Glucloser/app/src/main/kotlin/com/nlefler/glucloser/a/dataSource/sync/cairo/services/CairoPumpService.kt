package com.nlefler.glucloser.a.dataSource.sync.cairo.services

import com.nlefler.glucloser.a.models.BolusPattern
import com.nlefler.glucloser.a.models.SensorReading
import retrofit2.http.GET
import retrofit2.http.Path
import rx.Observable
import java.util.Date

/**
 * Created by nathan on 4/10/16.
 */
interface CairoPumpService {
    @GET("/pump/cgm/readingsBetween/{start}/{end}")
    fun cgmReadingsBetween(@Path("start") start: Date, @Path("end") end: Date): Observable<SensorReading>

    @GET("/pump/bolusPattern/current")
    fun currentBolusPattern(): Observable<BolusPattern>
}
