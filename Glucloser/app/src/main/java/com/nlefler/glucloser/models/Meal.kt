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
    override open var id: String = UUID.randomUUID().toString()
    override open var date: Date = Date()
    override open var bolusPattern: BolusPattern? = null
    override open var carbs: Int = 0
    override open var insulin: Float = 0f
    override open var beforeSugar: BloodSugar? = null
    override open var isCorrection: Boolean = false
    override open var foods: RealmList<Food> = RealmList()
    override open var place: Place? = null

    companion object {
        @Ignore
        public val ParseClassName: String = "Meal"

        @Ignore
        public val MealIdFieldName: String = "mealId"

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
