package com.nlefler.glucloser.models

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by Nathan Lefler on 5/8/15.
 */
@RealmClass
public open class Snack(
        @PrimaryKey override var primaryId: String = UUID.randomUUID().toString(),
        override var date: Date = Date(),
        override var bolusPattern: BolusPattern? = null,
        override var carbs: Int = 0,
        override var insulin: Float = 0f,
        override var beforeSugar: BloodSugar? = null,
        override var isCorrection: Boolean = false,
        override var foods: RealmList<Food> = RealmList()
    ) : RealmObject(), BolusEvent, Syncable {

    companion object {
        @Ignore
        val ModelName = "snacks"

        @Ignore
        val PrimaryKeyName = "primaryId"

        @Ignore
        val DateFieldName = "date"
    }
}
