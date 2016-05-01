package com.nlefler.glucloser.a.dataSource

import bolts.Task
import bolts.TaskCompletionSource
import com.nlefler.glucloser.a.dataSource.jsonAdapter.BolusPatternJsonAdapter
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.db.SQLStmts
import com.nlefler.glucloser.a.models.BolusPattern
import com.nlefler.glucloser.a.models.parcelable.BolusPatternParcelable
import com.nlefler.glucloser.a.models.BolusRate
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.util.*
import javax.inject.Inject

/**
 * Created by nathan on 9/19/15.
 */
class BolusPatternFactory @Inject constructor(val dbManager: DBManager, val bolusRateFactory: BolusRateFactory) {

    fun emptyPattern(): BolusPattern {
        return BolusPattern()
    }

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
            rates.add(BolusRate(rateParcelable.id, rateParcelable.ordinal, rateParcelable.carbsPerUnit, rateParcelable.startTime))
        }
        return BolusPattern(parcelable.id, rates)
    }

    private fun bolusPatternForId(id: String): Task<BolusPattern?> {
        if (id.isEmpty()) {
            return Task.forError<BolusPattern?>(Exception("Invalid ID"))
        }

        val task = TaskCompletionSource<BolusPattern?>()
        val query = SQLStmts.BolusPattern.RatesForID()
        dbManager.rawQuery(query, arrayOf(id), { cursor ->
            if (cursor == null) {
                task.setError(Exception("Unable to read db"))
                return@rawQuery
            }
            if (!cursor.moveToFirst()) {
                task.setError(Exception("No result for id and create not set"))
                return@rawQuery
            }
            val rates = ArrayList<BolusRate>()
            do {
                rates.add(BolusRate(query.getRateID(cursor), query.getOridinal(cursor),
                        query.getCarbsPerUnit(cursor), query.getStartTime(cursor)))
            } while (cursor.moveToNext())

            task.setResult(BolusPattern(id, rates))
            cursor.close()
        })
        return task.task
    }
}
