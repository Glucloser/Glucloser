package com.nlefler.glucloser.dataSource

import com.nlefler.glucloser.models.BolusRate
import com.parse.ParseObject
import com.parse.ParseQuery

import bolts.Task
import com.nlefler.glucloser.models.BolusRateParcelable
import io.realm.Realm
import java.util.*
import javax.inject.Inject

/**
 * Created by nathan on 9/1/15.
 */
public class BolusRateFactory {
    @Inject lateinit var realm: Realm

    public fun emptyRate(): BolusRate {
        var rate: BolusRate?
        realm.beginTransaction()
        rate = bolusRateForId("__glucloser_special_empty_bolus_rate", true)
        rate?.ordinal = 0
        rate?.rate = 0
        rate?.startTime = 0
        realm.commitTransaction()

        return rate!!
    }

    public fun bolusRateFromParcelable(parcelable: BolusRateParcelable): BolusRate {
        var rate: BolusRate?
        realm.beginTransaction()
        rate = bolusRateForId("", true)
        rate?.ordinal = parcelable.ordinal
        rate?.rate = parcelable.rate
        rate?.startTime = parcelable.startTime
        realm.commitTransaction()

        return rate!!
    }

    public fun parcelableFromBolusRate(rate: BolusRate): BolusRateParcelable {
        val parcel = BolusRateParcelable()
        parcel.ordinal = rate.ordinal
        parcel.rate = rate.rate
        parcel.startTime = rate.startTime

        return parcel
    }

    public fun bolusRateFromParseObject(parseObj: ParseObject): BolusRate? {
        if (!parseObj.getClassName().equals(BolusRate.ParseClassName)) {
            return null;
        }
        val patternId = parseObj.getString(BolusRate.IdFieldName) ?: return null

        var rate: BolusRate?
        realm.beginTransaction()
        rate = bolusRateForId(patternId, true)
        rate?.ordinal = parseObj.getInt(BolusRate.OridnalFieldName)
        rate?.rate = parseObj.getInt(BolusRate.RateFieldName)
        rate?.startTime = parseObj.getInt(BolusRate.StartTimeFieldName)
        realm.commitTransaction()

        return rate
    }

    public fun parseObjectFromBolusRate(rate: BolusRate): ParseObject {
        val prs = ParseObject.create(BolusRate.ParseClassName)
        prs.put(BolusRate.OridnalFieldName, rate.ordinal)
        prs.put(BolusRate.RateFieldName, rate.rate)
        prs.put(BolusRate.StartTimeFieldName, rate.startTime)

        return prs
    }

    private fun bolusRateForId(id: String, create: Boolean): BolusRate? {
        if (create && id.length() == 0) {
            val rate = realm.createObject<BolusRate>(BolusRate::class.java)
            return rate
        }

        val query = realm.where<BolusRate>(BolusRate::class.java)

        query.equalTo(BolusRate.IdFieldName, id)
        var result: BolusRate? = query.findFirst()

        if (result == null && create) {
            result = realm.createObject<BolusRate>(BolusRate::class.java)
            result!!.NLID = id
        }

        return result
    }
}
