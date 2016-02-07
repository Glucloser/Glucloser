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
        @PrimaryKey open var primaryId: String = UUID.randomUUID().toString(),
        open var carbs: Int = 0,
        open var foodName: String = ""
    ) : RealmObject() {

    companion object {
        @Ignore
        val ModelName: String = "foods"

        @Ignore
        val IdFieldName: String = "primaryId"
    }
}
