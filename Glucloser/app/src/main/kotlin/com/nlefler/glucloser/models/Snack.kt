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
    ) : RealmObject(), BolusEvent {

    companion object {
        @Ignore
        public var ParseClassName: String = "Snack"

        @Ignore
        public var IdFieldName: String = "primaryId"

        @Ignore
        public var SnackDateFieldName: String = "date"

        @Ignore
        public var CarbsFieldName: String = "carbs"

        @Ignore
        public var InsulinFieldName: String = "insulin"

        @Ignore
        public var BeforeSugarFieldName: String = "beforeSugar"

        @Ignore
        public var CorrectionFieldName: String = "correction"

        @Ignore
        public var FoodListFieldName: String = "foods"
    }
}
