package com.nlefler.glucloser.a.dataSource

import com.nlefler.glucloser.a.models.BolusRate

import com.nlefler.glucloser.a.dataSource.jsonAdapter.BolusRateJsonAdapter
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.models.BolusRateEntity
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

    fun entityFrom(rate: BolusRate): BolusRateEntity {
        if (rate is BolusRateEntity) {
            return rate
        }
        val br = BolusRateEntity()
        br.primaryId = rate.primaryId
        br.ordinal = rate.ordinal
        br.carbsPerUnit = rate.carbsPerUnit
        br.startTime = rate.startTime
        return br
    }

    fun parcelableFrom(rate: BolusRate): BolusRateParcelable {
        if (rate is BolusRateParcelable) {
            return rate
        }
        val parcel = BolusRateParcelable()
        parcel.ordinal = rate.ordinal
        parcel.carbsPerUnit = rate.carbsPerUnit
        parcel.startTime = rate.startTime

        return parcel
    }

    fun jsonAdapter(): JsonAdapter<BolusRateEntity> {
        return Moshi.Builder()
                .add(BolusRateJsonAdapter())
                .build()
                .adapter(BolusRateEntity::class.java)
    }

    private fun bolusRateForId(id: String): Observable<Result<BolusRateEntity>> {
        if (id.isEmpty()) {
            return Observable.error(Exception("Invalid Id"))
        }

        return dbManager.data.select(BolusRateEntity::class).where(BolusRate::primaryId.eq(id)).get().toSelfObservable()
    }

}
