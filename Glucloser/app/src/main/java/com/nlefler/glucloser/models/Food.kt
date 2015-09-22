package com.nlefler.glucloser.models

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by Nathan Lefler on 5/16/15.
 */
@RealmClass
public open data class Food : RealmObject() {
    public open var foodId: String = UUID.randomUUID().toString()
    public open var carbs: Int = 0
    public open var name: String = ""

    companion object {
        Ignore
        public val ParseClassName: String = "Food"

        Ignore
        public val FoodIdFieldName: String = "foodId"

        Ignore
        public val CarbsFieldName: String = "carbs"

        Ignore
        public val FoodNameFieldName: String = "name"
    }
}
