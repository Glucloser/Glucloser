package com.nlefler.glucloser.a.dataSource

import com.nlefler.glucloser.a.dataSource.sync.cairo.CairoServices
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoPumpService
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.models.BloodSugar
import com.nlefler.glucloser.a.models.BloodSugarEntity
import com.nlefler.glucloser.a.models.SugarReading
import com.nlefler.glucloser.a.models.parcelable.BloodSugarParcelable
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.requery.kotlin.desc
import io.requery.meta.Attribute
import io.requery.meta.AttributeDelegate
import io.requery.meta.Type
import rx.Observable
import rx.schedulers.Schedulers
import java.util.*

import javax.inject.Inject

/**
 * Created by Nathan Lefler on 1/4/15.
 */
class BloodSugarFactory @Inject constructor(val dbManager: DBManager, val cairoServices: CairoServices) {
    private val LOG_TAG = "BloodSugarFactory"

    fun lastBloodSugarFromCGM(): Observable<SugarReading> {
        val dbObservable = dbManager.data.select(BloodSugarEntity::class)
                .orderBy(BloodSugarEntity::recordedDate.desc())
                .limit(1).get().toObservable()

        // TODO(nl): use diff endpoint for last sugar
        val date = Date()
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.MINUTE, -30)
        val endDate = cal.time

       val network = cairoServices.pumpService().
               cgmReadingsBetween(CairoPumpService.CGMReadingsBetweenBody(date, endDate))

       return Observable.merge(dbObservable, network).subscribeOn(Schedulers.io()) as Observable<SugarReading>
    }

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
