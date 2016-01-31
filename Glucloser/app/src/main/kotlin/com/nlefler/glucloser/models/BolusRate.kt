package com.nlefler.glucloser.models

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by nathan on 8/30/15.
 */
@RealmClass
public open class BolusRate(
        @PrimaryKey public open var primaryId: String = UUID.randomUUID().toString(),
        public open var ordinal: Int = 0,
        public open var carbsPerUnit: Int = 0,
        public open var startTime: Int = 0
    ) : RealmObject() {

    companion object {
        @Ignore
        val ModelName = "CurrentCarbRatio"

        @Ignore
        val IdFieldName = "primaryId"

        @Ignore
        val OridnalFieldName = "ordinal"

        @Ignore
        val CarbsPerUnitFieldName = "amount"

        @Ignore
        val StartTimeFieldName = "startTime"
    }
}
