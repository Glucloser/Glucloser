package com.nlefler.glucloser.models

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by Nathan Lefler on 12/11/14.
 */
@RealmClass
public open class Meal : RealmObject(), BolusEvent, HasPlace {
    override var id: String = UUID.randomUUID().toString()
    override var date: Date = Date()
    override var bolusPattern: BolusPattern? = null
    override var carbs: Int = 0
    override var insulin: Float = 0f
    override var beforeSugar: BloodSugar? = null
    override var isCorrection: Boolean = false
    override var foods: RealmList<Food> = RealmList()
    override var place: Place? = null

    companion object {
        @Ignore
        public val ParseClassName: String = "Meal"

        @Ignore
        public val MealIdFieldName: String = "id"

        @Ignore
        public val MealDateFieldName: String = "date"

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
