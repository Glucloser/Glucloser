package com.nlefler.glucloser.a.dataSource.sync.cairo.services

import com.nlefler.glucloser.a.models.BolusPattern
import com.nlefler.glucloser.a.models.BolusPatternEntity
import com.nlefler.glucloser.a.models.SensorReading
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import rx.Observable
import java.util.Date

/**
 * Created by nathan on 4/10/16.
 */
interface CairoPumpService {
    data class CGMReadingsBetweenBody(val start: Date, val end: Date) {}
    data class CGMReadingsBetweenResponse(val readings: Array<SensorReading>) {
        override fun equals(other: Any?): Boolean {
            if (!(other is CGMReadingsBetweenResponse)) {
                return false
            }
            return other.readings.equals(readings)
        }

        override fun hashCode(): Int {
            return readings.hashCode()
        }
    }
    @POST("pump/cgm/readingsBetween")
    fun cgmReadingsBetween(@Body body: CGMReadingsBetweenBody): Observable<CGMReadingsBetweenResponse>

    @GET("pump/bolusPattern/current")
    fun currentBolusPattern(): Observable<BolusPatternEntity>
}
