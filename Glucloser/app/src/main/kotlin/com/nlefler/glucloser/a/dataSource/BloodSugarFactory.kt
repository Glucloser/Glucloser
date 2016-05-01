package com.nlefler.glucloser.a.dataSource

import bolts.Task
import bolts.TaskCompletionSource
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.db.SQLStmts
import com.nlefler.glucloser.a.models.BloodSugar
import com.nlefler.glucloser.a.models.parcelable.BloodSugarParcelable
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

import javax.inject.Inject

/**
 * Created by Nathan Lefler on 1/4/15.
 */
class BloodSugarFactory @Inject constructor(val dbManager: DBManager) {
    private val LOG_TAG = "BloodSugarFactory"

    fun areBloodSugarsEqual(sugar1: BloodSugar?, sugar2: BloodSugar?): Boolean {
        if (sugar1 == null || sugar2 == null) {
            return false
        }

        val valueOk = sugar1.value == sugar2.value
        val dateOK = sugar1.recordedDate == sugar2.recordedDate

        return valueOk && dateOK
    }

    fun bloodSugarFromParcelable(parcelable: BloodSugarParcelable): BloodSugar {
        return BloodSugar(parcelable.id, parcelable.value, parcelable.date)
    }

    fun parcelableFromBloodSugar(sugar: BloodSugar): BloodSugarParcelable {
        val parcelable = BloodSugarParcelable()
        parcelable.id = sugar.primaryId
        parcelable.value = sugar.value
        parcelable.date = sugar.recordedDate
        return parcelable
    }

    fun jsonAdapter(): JsonAdapter<BloodSugar> {
        return Moshi.Builder()
                .build()
                .adapter(BloodSugar::class.java)
    }

    private fun bloodSugarForBloodSugarId(id: String, create: Boolean): Task<BloodSugar?> {
        if (create && id.isEmpty()) {
            return Task.forResult(BloodSugar())
        }

        var task = TaskCompletionSource<BloodSugar?>()
        val query = SQLStmts.BloodSugar.ForID()
        dbManager.query(query, arrayOf(id), { cursor ->
            if (cursor == null) {
                task.setError(Exception("Unable to read db"))
                return@query
            }
            if (!cursor.moveToFirst()) {
                if (create) {
                    task.setResult(BloodSugar(id))
                    return@query
                }
                else {
                    task.setError(Exception("No result for id and create not set"))
                    return@query
                }
            }
            task.setResult(BloodSugar(id, query.getValue(cursor), query.getDate(cursor)))
        })
        return task.task
    }
}
