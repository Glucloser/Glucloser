package com.nlefler.glucloser.models

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by Nathan Lefler on 12/11/14.
 */
@RealmClass
public open class Meal(
        @PrimaryKey override var primaryId: String = UUID.randomUUID().toString(),
        override var date: Date = Date(),
        override var bolusPattern: BolusPattern? = null,
        override var carbs: Int = 0,
        override var insulin: Float = 0f,
        override var beforeSugar: BloodSugar? = null,
        override var isCorrection: Boolean = false,
        override var foods: RealmList<Food> = RealmList(),
        override var place: Place? = null
    ) : RealmObject(), BolusEvent, HasPlace {

    companion object {
        @Ignore
        public val ModelName: String = "meals"

        @Ignore
        public val IdFieldName: String = "primaryId"

        @Ignore
        public val DateFieldName: String = "date"

        @Ignore
        public val PlaceFieldName: String = "place"

        @Ignore
        public val CarbsFieldName: String = "carbs"

        @Ignore
        public val InsulinFieldName: String = "insulin"

        @Ignore
        public val BeforeSugarFieldName: String = "beforeSugar"

        @Ignore
        public val CorrectionFieldName: String = "correction"

        @Ignore
        public val FoodListFieldName: String = "foods"

        @Ignore
        public val BolusPatternFieldName: String = "bolusPattern"
    }
}
