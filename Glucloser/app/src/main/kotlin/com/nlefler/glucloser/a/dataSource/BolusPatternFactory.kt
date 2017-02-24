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

    // TODO(nl): inject
    val bolusRateJsonAdapter: BolusRateJsonAdapter = BolusRateJsonAdapter()

    /**
     * Fetches from db and network and returns both results.
     */
    fun currentBolusPattern(): Observable<BolusPattern> {
        val dbObservable = dbManager.data.select(BolusPatternEntity::class)
                .orderBy(BolusPatternEntity::updatedOn.desc())
                .limit(1).get().toObservable()

        val networkObservable = services.pumpService().currentBolusPattern()
        return Observable.merge(dbObservable, networkObservable)
    }

    fun entityFrom(pattern: BolusPattern): BolusPatternEntity {
        if (pattern is BolusPatternEntity) {
            return pattern
        }
        val entity = BolusPatternEntity()
        entity.primaryId = pattern.primaryId
        entity.updatedOn = pattern.updatedOn
        entity.rates.addAll(pattern.rates.map { r -> bolusRateFactory.entityFrom(r)})
        return entity
    }

    fun parcelableFrom(pattern: BolusPattern): BolusPatternParcelable {
        if (pattern is BolusPatternParcelable) {
            return pattern
        }

        val parcelable = BolusPatternParcelable(bolusRateFactory)
        parcelable.primaryId = pattern.primaryId
        parcelable.updatedOn = pattern.updatedOn
        parcelable.rates = pattern.rates.map { r -> bolusRateFactory.parcelableFrom(r)}.toMutableSet()
        return parcelable
    }

    fun jsonAdapter(): JsonAdapter<BolusPatternEntity> {
        return Moshi.Builder()
                .add(BolusRateJsonAdapter())
                .add(BolusPatternJsonAdapter(bolusRateFactory))
                .build()
                .adapter(BolusPatternEntity::class.java)
    }

    private fun bolusPatternForId(id: String): Observable<Result<BolusPatternEntity>> {
        if (id.isEmpty()) {
            return Observable.error(Exception("Invalid ID"))
        }
        return dbManager.data.select(BolusPatternEntity::class).where(BolusPattern::primaryId.eq(id)).get().toSelfObservable()
    }
}
