package com.nlefler.glucloser.a.dataSource

import com.nlefler.glucloser.a.models.BolusRate

import com.nlefler.glucloser.a.dataSource.jsonAdapter.BolusRateJsonAdapter
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.models.parcelable.BolusRateParcelable
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.requery.kotlin.eq
import io.requery.query.Result
import rx.Observable
import javax.inject.Inject

/**
 * Created by nathan on 9/1/15.
 */
class BolusRateFactory @Inject constructor(val dbManager: DBManager) {

    fun emptyRate(): BolusRate {
        return BolusRate("__EMPTY", 0, 0, 0)
    }

    fun bolusRateFromParcelable(parcelable: BolusRateParcelable): BolusRate {
        return BolusRate(parcelable.id, parcelable.ordinal, parcelable.carbsPerUnit, parcelable.startTime)
    }

    fun parcelableFromBolusRate(rate: BolusRate): BolusRateParcelable {
        val parcel = BolusRateParcelable()
        parcel.ordinal = rate.ordinal
        parcel.carbsPerUnit = rate.carbsPerUnit
        parcel.startTime = rate.startTime

        return parcel
    }

    fun jsonAdapter(): JsonAdapter<BolusRate> {
        return Moshi.Builder()
                .add(BolusRateJsonAdapter())
                .build()
                .adapter(BolusRate::class.java)
    }

    private fun bolusRateForId(id: String, create: Boolean): Observable<Result<BolusRate>> {
        if (id.isEmpty()) {
            return Observable.error(Exception("Invalid Id"))
        }

        return dbManager.data.select(BolusRate::class).where(BolusRate::primaryId.eq(id)).get().toSelfObservable()
    }

}
