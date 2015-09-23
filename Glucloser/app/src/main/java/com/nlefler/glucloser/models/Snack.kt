package com.nlefler.glucloser.models

import java.util.Date

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.RealmClass

/**
 * Created by Nathan Lefler on 5/8/15.
 */
@RealmClass
public abstract data class Snack : RealmObject(), BolusEvent {

    companion object {
        @Ignore
        public var ParseClassName: String = "Snack"

        @Ignore
        public var SnackIdFieldName: String = "snackId"

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
