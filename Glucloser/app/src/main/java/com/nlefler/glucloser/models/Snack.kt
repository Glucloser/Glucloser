package com.nlefler.glucloser.models

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by Nathan Lefler on 5/8/15.
 */
@RealmClass
public open class Snack : RealmObject(), BolusEvent {
    override open var id: String = UUID.randomUUID().toString()
    override open var date: Date = Date()
    override open var bolusPattern: BolusPattern? = null
    override open var carbs: Int = 0
    override open var insulin: Float = 0f
    override open var beforeSugar: BloodSugar? = null
    override open var isCorrection: Boolean = false
    override open var foods: RealmList<Food> = RealmList<Food>()

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
