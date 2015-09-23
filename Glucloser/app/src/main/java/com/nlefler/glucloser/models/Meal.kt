package com.nlefler.glucloser.models

import java.util.Date

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.RealmClass

/**
 * Created by Nathan Lefler on 12/11/14.
 */
@RealmClass
public abstract data class Meal : RealmObject(), BolusEvent, HasPlace {

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
