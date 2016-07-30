package com.nlefler.glucloser.a.dataSource

import com.nlefler.glucloser.a.dataSource.jsonAdapter.BolusPatternJsonAdapter
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.models.BolusPattern
import com.nlefler.glucloser.a.models.BolusPatternEntity
import com.nlefler.glucloser.a.models.parcelable.BolusPatternParcelable
import com.nlefler.glucloser.a.models.BolusRate
import com.nlefler.glucloser.a.models.BolusRateEntity
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.requery.kotlin.eq
import io.requery.query.Result
import rx.Observable
import java.util.ArrayList
import javax.inject.Inject

/**
 * Created by nathan on 9/19/15.
 */
class BolusPatternFactory @Inject constructor(val dbManager: DBManager, val bolusRateFactory: BolusRateFactory) {

    fun parcelableFromBolusPattern(pattern: BolusPattern): BolusPatternParcelable {
        val parcel = BolusPatternParcelable()
        for (rate in pattern.rates) {
            parcel.rates.add(bolusRateFactory.parcelableFromBolusRate(rate))
        }
        return parcel
    }

    fun jsonAdapter(): JsonAdapter<BolusPattern> {
        return Moshi.Builder()
                .add(BolusPatternJsonAdapter())
                .build()
                .adapter(BolusPattern::class.java)
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

    private fun bolusPatternForId(id: String): Observable<Result<BolusPattern>> {
        if (id.isEmpty()) {
            return Observable.error(Exception("Invalid ID"))
        }
        return dbManager.data.select(BolusPattern::class).where(BolusPattern::primaryId.eq(id)).get().toSelfObservable()
    }
}
