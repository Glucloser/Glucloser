package com.nlefler.glucloser.models

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by Nathan Lefler on 5/16/15.
 */
@RealmClass
public open class Food(
        @PrimaryKey public open var primaryId: String = UUID.randomUUID().toString(),
        public open var carbs: Int = 0,
        public open var foodName: String = ""
    ) : RealmObject() {

    companion object {
        @Ignore
        public val ModelName: String = "Food"

        @Ignore
        public val IdFieldName: String = "primaryId"

        @Ignore
        public val CarbsFieldName: String = "carbs"

        @Ignore
        public val NameFieldName: String = "name"
    }
}
