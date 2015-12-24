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
public class BolusRateFactory @Inject constructor(val realmManager: RealmManager) {

    public fun emptyRate(): Task<BolusRate?> {
        return bolusRateForId("__glucloser_special_empty_bolus_rate", true).continueWithTask { task ->
            if (task.isFaulted) {
                return@continueWithTask task
            }
            val realmTask = TaskCompletionSource<BolusRate?>()
            realmManager.executeTransaction(Realm.Transaction { realm ->
                val rate = task.result
                rate?.ordinal = 0
                rate?.carbsPerUnit = 0
                rate?.startTime = 0
                realmTask.trySetResult(rate)
            }, realmTask.task)
        }
    }

    public fun bolusRateFromParcelable(parcelable: BolusRateParcelable): Task<BolusRate?> {
        return bolusRateForId("", true).continueWithTask { task ->
            if (task.isFaulted) {
                return@continueWithTask task
            }
            val rate = task.result
            val realmTask = TaskCompletionSource<BolusRate?>()
            realmManager.executeTransaction(Realm.Transaction { realm ->
                rate?.ordinal = parcelable.ordinal
                rate?.carbsPerUnit = parcelable.carbsPerUnit
                rate?.startTime = parcelable.startTime
                realmTask.trySetResult(rate)
            }, realmTask.task)
        }
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

        return bolusRateForId(patternId, true).continueWithTask { task ->
            if (task.isFaulted) {
                return@continueWithTask task
            }
            val rate = task.result
            val realmTask = TaskCompletionSource<BolusRate?>()
            realmManager.executeTransaction(Realm.Transaction { realm ->
                rate?.ordinal = parseObj.getInt(BolusRate.OridnalFieldName)
                rate?.carbsPerUnit = parseObj.getInt(BolusRate.CarbsPerUnitFieldName)
                rate?.startTime = parseObj.getInt(BolusRate.StartTimeFieldName)
                realmTask.trySetResult(rate)
            }, realmTask.task)
        }
    }

    public fun parseObjectFromBolusRate(rate: BolusRate): ParseObject {
        val prs = ParseObject.create(BolusRate.ParseClassName)
        prs.put(BolusRate.OridnalFieldName, rate.ordinal)
        prs.put(BolusRate.CarbsPerUnitFieldName, rate.carbsPerUnit)
        prs.put(BolusRate.StartTimeFieldName, rate.startTime)

        return prs
    }

    private fun bolusRateForId(id: String, create: Boolean): Task<BolusRate?> {
        val rateTask = TaskCompletionSource<BolusRate?>()
        return realmManager.executeTransaction(Realm.Transaction { realm ->
            if (create && id.length == 0) {
                val rate = realm.createObject<BolusRate>(BolusRate::class.java)
                rateTask.trySetResult(rate)
                return@Transaction
            }

            val query = realm.where<BolusRate>(BolusRate::class.java)

            query?.equalTo(BolusRate.IdFieldName, id)
            var rate = query?.findFirst()

            if (rate == null && create) {
                rate = realm.createObject<BolusRate>(BolusRate::class.java)
                rate!!.NLID = id
                rateTask.trySetResult(rate)
            }
            else if (rate == null) {
                rateTask.trySetError(Exception("No Rate found for id ${id} and create is false"))
            }
            else {
                rateTask.trySetResult(rate)
            }
        }, rateTask.task)
    }

}
