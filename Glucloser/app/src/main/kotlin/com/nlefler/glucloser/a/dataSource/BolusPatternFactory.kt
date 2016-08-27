package com.nlefler.glucloser.a.dataSource

import com.nlefler.glucloser.a.dataSource.jsonAdapter.BolusPatternJsonAdapter
import com.nlefler.glucloser.a.dataSource.jsonAdapter.BolusRateJsonAdapter
import com.nlefler.glucloser.a.dataSource.sync.cairo.CairoServices
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoPumpService
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.models.BolusPattern
import com.nlefler.glucloser.a.models.BolusPatternEntity
import com.nlefler.glucloser.a.models.parcelable.BolusPatternParcelable
import com.nlefler.glucloser.a.models.BolusRate
import com.nlefler.glucloser.a.models.BolusRateEntity
import com.nlefler.glucloser.a.models.json.BolusRateJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.requery.kotlin.desc
import io.requery.kotlin.eq
import io.requery.query.Result
import rx.Observable
import java.util.ArrayList
import javax.inject.Inject

/**
 * Created by nathan on 9/19/15.
 */
class BolusPatternFactory @Inject constructor(val dbManager: DBManager,
                                              val bolusRateFactory: BolusRateFactory,
                                              val services: CairoServices) {

    /**
     * Fetches from db and network and returns both results.
     */
    fun currentBolusPattern(): Observable<BolusPatternEntity> {
//        val dbObservable = dbManager.data.select(BolusPatternEntity::class)
//                .orderBy(BolusPatternEntity::updatedOn.desc())
//                .limit(1).get().toObservable()
        var dbObservable: Observable<BolusPatternEntity> = Observable.empty()
        dbManager.data.invoke {
            val result = select(BolusPatternEntity::class)
            val f = result.get().firstOrNull()
            if (f != null) {
                dbObservable = Observable.just(f)
            }
        }
        val networkObservable = services.pumpService().currentBolusPattern()
        return Observable.merge(dbObservable, networkObservable)
    }

    fun parcelableFromBolusPattern(pattern: BolusPattern): BolusPatternParcelable {
        val parcel = BolusPatternParcelable()
        for (rate in pattern.rates) {
            parcel.rates.add(bolusRateFactory.parcelableFromBolusRate(rate))
        }
        return parcel
    }

    fun jsonAdapter(): JsonAdapter<BolusPatternEntity> {
        return Moshi.Builder()
                .add(BolusRateJsonAdapter())
                .add(BolusPatternJsonAdapter())
                .build()
                .adapter(BolusPatternEntity::class.java)
    }

    fun bolusPatternFromParcelable(parcelable: BolusPatternParcelable): BolusPattern {
        val rates = ArrayList<BolusRate>()
        parcelable.rates.forEach { rateParcelable ->
            val br = BolusRateEntity()
            br.primaryId = rateParcelable.id
            br.ordinal = rateParcelable.ordinal
            br.carbsPerUnit = rateParcelable.carbsPerUnit
            br.startTime = rateParcelable.startTime
            rates.add(br)
        }
        val bp = BolusPatternEntity()
        bp.primaryId = parcelable.id
        bp.rates = rates
        return bp
    }

    private fun bolusPatternForId(id: String): Observable<Result<BolusPatternEntity>> {
        if (id.isEmpty()) {
            return Observable.error(Exception("Invalid ID"))
        }
        return dbManager.data.select(BolusPatternEntity::class).where(BolusPattern::primaryId.eq(id)).get().toSelfObservable()
    }
}
