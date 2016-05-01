package com.nlefler.glucloser.a.dataSource

import bolts.Continuation
import com.nlefler.glucloser.a.models.BolusRate

import bolts.Task
import bolts.TaskCompletionSource
import com.nlefler.glucloser.a.dataSource.jsonAdapter.BolusRateJsonAdapter
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.db.SQLStmts
import com.nlefler.glucloser.a.models.parcelable.BolusRateParcelable
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import javax.inject.Inject

/**
 * Created by nathan on 9/1/15.
 */
class BolusRateFactory @Inject constructor(val dbManager: DBManager) {

    fun emptyRate(): BolusRate {
        return BolusRate()
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

    private fun bolusRateForId(id: String, create: Boolean): Task<BolusRate> {
        if (id.isEmpty()) {
            return Task.forError<BolusRate>(Exception("Invalid ID"))
        }

        val task = TaskCompletionSource<BolusRate>()
        val query = SQLStmts.BolusRate.ForID()
        dbManager.query(query, arrayOf(id), { cursor ->
            if (cursor == null) {
                task.setError(Exception("Unable to read db"))
                return@query
            }
            if (!cursor.moveToFirst()) {
                task.setError(Exception("No result for id and create not set"))
                return@query
            }
            task.setResult(BolusRate(id, query.getOridnal(cursor), query.getCarbsPerUnit(cursor),
                    query.getStartTime(cursor)))
            cursor.close()
        })
        return task.task
    }

}
