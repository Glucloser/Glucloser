package com.nlefler.glucloser.dataSource

import com.nlefler.glucloser.models.BolusRate
import com.parse.ParseObject
import com.parse.ParseQuery

import bolts.Task
import bolts.TaskCompletionSource
import com.nlefler.glucloser.models.BolusRateParcelable
import io.realm.Realm
import java.util.*
import javax.inject.Inject

/**
 * Created by nathan on 9/1/15.
 */
public class BolusRateFactory @Inject constructor(val realm: Realm) {

    public fun emptyRate(): Task<BolusRate> {
        val task = TaskCompletionSource<BolusRate>()

        realm.executeTransaction { realm ->
            val rate = bolusRateForId("__glucloser_special_empty_bolus_rate", true)
            rate?.ordinal = 0
            rate?.carbsPerUnit = 0
            rate?.startTime = 0

            task.trySetResult(rate)
        }

        return task.task
    }

    public fun bolusRateFromParcelable(parcelable: BolusRateParcelable): Task<BolusRate> {
        val task = TaskCompletionSource<BolusRate>()

        realm.executeTransaction { realm ->
            val rate = bolusRateForId("", true)

            rate?.ordinal = parcelable.ordinal
            rate?.carbsPerUnit = parcelable.carbsPerUnit
            rate?.startTime = parcelable.startTime

            task.trySetResult(rate)
        }

        return task.task
    }

    public fun parcelableFromBolusRate(rate: BolusRate): BolusRateParcelable {
        val parcel = BolusRateParcelable()
        parcel.ordinal = rate.ordinal
        parcel.carbsPerUnit = rate.carbsPerUnit
        parcel.startTime = rate.startTime

        return parcel
    }

    public fun bolusRateFromParseObject(parseObj: ParseObject): Task<BolusRate?> {
        if (!parseObj.getClassName().equals(BolusRate.ParseClassName)) {
            return Task.forError(Exception("Invalid ParseObject"));
        }
        val patternId = parseObj.getString(BolusRate.IdFieldName) ?: return Task.forError(Exception("Invalid ParseObject"))

        val task = TaskCompletionSource<BolusRate?>()

        realm.executeTransaction { realm ->
            val rate = bolusRateForId(patternId, true)
            rate?.ordinal = parseObj.getInt(BolusRate.OridnalFieldName)
            rate?.carbsPerUnit = parseObj.getInt(BolusRate.CarbsPerUnitFieldName)
            rate?.startTime = parseObj.getInt(BolusRate.StartTimeFieldName)

            task.trySetResult(rate)
        }

        return task.task
    }

    public fun parseObjectFromBolusRate(rate: BolusRate): ParseObject {
        val prs = ParseObject.create(BolusRate.ParseClassName)
        prs.put(BolusRate.OridnalFieldName, rate.ordinal)
        prs.put(BolusRate.CarbsPerUnitFieldName, rate.carbsPerUnit)
        prs.put(BolusRate.StartTimeFieldName, rate.startTime)

        return prs
    }

    private fun bolusRateForId(id: String, create: Boolean): BolusRate? {
        if (create && id.length == 0) {
            val rate = realm.createObject<BolusRate>(BolusRate::class.java)
            return rate
        }

        val query = realm.where<BolusRate>(BolusRate::class.java)

        query?.equalTo(BolusRate.IdFieldName, id)
        var result: BolusRate? = query?.findFirst()

        if (result == null && create) {
            result = realm.createObject<BolusRate>(BolusRate::class.java)
            result!!.NLID = id
        }

        return result
    }
}
