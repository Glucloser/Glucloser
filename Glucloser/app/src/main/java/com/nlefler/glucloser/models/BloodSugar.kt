package com.nlefler.glucloser.models

import java.util.Date

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.RealmClass

/**
 * Created by Nathan Lefler on 1/4/15.
 */
@RealmClass
public open data class BloodSugar : RealmObject() {
    public open var id: String? = null
    public open var value: Int = 0
    public open var date: Date? = null

    companion object {
        @Ignore
        public val ParseClassName: String = "BloodSugar"

        @Ignore
        public val IdFieldName: String = "id"

        @Ignore
        public val ValueFieldName: String = "value"

        @Ignore
        public val DateFieldName: String = "date"
    }
}
