package com.nlefler.glucloser.models

import com.nlefler.glucloser.models.BolusRate
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.RealmClass

/**
 * Created by nathan on 9/19/15.
 */
@RealmClass
public open class BolusPattern : RealmObject() {
    public open var rateCount: Int? = null
    public open var rates = RealmList<BolusRate>()

    companion object {
        @Ignore
        val ParseClassName = "BolusPattern"

        @Ignore
        val RateCountFieldName = "rateCount"

        @Ignore
        val RatesFieldName = "rates"
    }
}
