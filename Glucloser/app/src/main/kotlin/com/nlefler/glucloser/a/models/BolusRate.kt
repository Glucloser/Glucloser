package com.nlefler.glucloser.a.models

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by nathan on 8/30/15.
 */
@RealmClass
open class BolusRate(
        @PrimaryKey open var primaryId: String = UUID.randomUUID().toString(),
        open var ordinal: Int = 0,
        open var carbsPerUnit: Int = 0,
        open var startTime: Int = 0
    ) : RealmObject() {

    companion object {
        @Ignore
        val ModelName = "bolusRates"

        @Ignore
        val IdFieldName = "primaryId"
    }
}
