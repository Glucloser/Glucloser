package com.nlefler.glucloser.models

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by nathan on 8/30/15.
 */
@RealmClass
public open class BolusRate : RealmObject() {
    public open var ordinal: Int? = null
    public open var rate: Int? = null
    public open var startTime: Int? = null

    companion object {
        @Ignore
        val ParseClassName = "BolusRate"

        @Ignore
        val OridnalFieldName = "ordinal"

        @Ignore
        val RateFieldName = "rate"

        @Ignore
        val StartTimeFieldName = "startTime"
    }
}
