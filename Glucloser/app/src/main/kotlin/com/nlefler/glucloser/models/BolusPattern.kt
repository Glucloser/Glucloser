package com.nlefler.glucloser.models

import com.nlefler.glucloser.models.BolusRate
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by nathan on 9/19/15.
 */
@RealmClass
open class BolusPattern(
        @PrimaryKey open var primaryId: String = UUID.randomUUID().toString(),
        open var rates: RealmList<BolusRate> = RealmList<BolusRate>()
    ) : RealmObject() {

    companion object {
        @Ignore
        val ModelName = "bolusPatterns"

        @Ignore
        val IdFieldName = "primaryId"
    }
}
