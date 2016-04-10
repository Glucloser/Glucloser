package com.nlefler.glucloser.a.models

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by Nathan Lefler on 1/4/15.
 */
@RealmClass
open class BloodSugar(
        @PrimaryKey open var primaryId: String = UUID.randomUUID().toString(),
        open var value: Int = 0,
        open var recordedDate: Date = Date()
    ) : RealmObject() {

    companion object {
        @Ignore
        val ModelName: String = "bloodSugars"

        @Ignore
        val IdFieldName: String = "primaryId"
    }
}
