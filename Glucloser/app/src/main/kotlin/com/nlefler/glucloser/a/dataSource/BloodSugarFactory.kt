package com.nlefler.glucloser.a.dataSource

import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.models.BloodSugar
import com.nlefler.glucloser.a.models.BloodSugarEntity
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

        val valueOk = sugar1.readingValue == sugar2.readingValue
        val dateOK = sugar1.recordedDate == sugar2.recordedDate

        return valueOk && dateOK
    }

    fun bloodSugarFromParcelable(parcelable: BloodSugarParcelable): BloodSugar {
        val bs = BloodSugarEntity()
        bs.primaryId = parcelable.id
        bs.readingValue = parcelable.value
        bs.recordedDate = parcelable.date
        return bs
    }

    fun parcelableFromBloodSugar(sugar: BloodSugar): BloodSugarParcelable {
        val parcelable = BloodSugarParcelable()
        parcelable.id = sugar.primaryId
        parcelable.value = sugar.readingValue
        parcelable.date = sugar.recordedDate
        return parcelable
    }

    fun jsonAdapter(): JsonAdapter<BloodSugarEntity> {
        return Moshi.Builder()
                .build()
                .adapter(BloodSugarEntity::class.java)
    }
}
