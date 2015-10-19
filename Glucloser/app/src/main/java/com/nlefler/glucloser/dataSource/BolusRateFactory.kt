package com.nlefler.glucloser.dataSource

import com.nlefler.glucloser.models.BolusRate
import com.parse.ParseObject
import com.parse.ParseQuery

import bolts.Task
import com.nlefler.glucloser.models.BolusRateParcelable
import io.realm.Realm
import java.util.*

/**
 * Created by nathan on 9/1/15.
 */
public class BolusRateFactory {
    companion object {
        public fun EmptyRate(): BolusRate {
            var rate: BolusRate? = null
            Realm.getDefaultInstance().beginTransaction()
            rate = BolusRateForId("__glucloser_special_empty_bolus_rate", Realm.getDefaultInstance(), true)
            rate?.ordinal = 0
            rate?.rate = 0
            rate?.startTime = 0
            Realm.getDefaultInstance().commitTransaction()

            return rate!!
        }

        public fun BolusRateFromParcelable(parcelable: BolusRateParcelable): BolusRate {
            var rate: BolusRate? = null
            Realm.getDefaultInstance().beginTransaction()
            rate = BolusRateForId("", Realm.getDefaultInstance(), true)
            rate?.ordinal = parcelable.ordinal
            rate?.rate = parcelable.rate
            rate?.startTime = parcelable.startTime
            Realm.getDefaultInstance().commitTransaction()

            return rate!!
        }

        public fun ParcelableFromBolusRate(rate: BolusRate): BolusRateParcelable {
            val parcel = BolusRateParcelable()
            parcel.ordinal = rate.ordinal
            parcel.rate = rate.rate
            parcel.startTime = rate.startTime

            return parcel
        }

        public fun BolusRateFromParseObject(parseObj: ParseObject): BolusRate? {
            if (!parseObj.getClassName().equals(BolusRate.ParseClassName)) {
                return null;
            }
            val patternId = parseObj.getString(BolusRate.IdFieldName) ?: return null

            var rate: BolusRate? = null
            Realm.getDefaultInstance().beginTransaction()
            rate = BolusRateForId(patternId, Realm.getDefaultInstance(), true)
            rate?.ordinal = parseObj.getInt(BolusRate.OridnalFieldName)
            rate?.rate = parseObj.getInt(BolusRate.RateFieldName)
            rate?.startTime = parseObj.getInt(BolusRate.StartTimeFieldName)
            Realm.getDefaultInstance().commitTransaction()

            return rate
        }

        public fun ParseObjectFromBolusRate(rate: BolusRate): ParseObject {
            val prs = ParseObject.create(BolusRate.ParseClassName)
            prs.put(BolusRate.OridnalFieldName, rate.ordinal)
            prs.put(BolusRate.RateFieldName, rate.rate)
            prs.put(BolusRate.StartTimeFieldName, rate.startTime)

            return prs
        }

        private fun BolusRateForId(id: String, realm: Realm, create: Boolean): BolusRate? {
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
}
