package com.nlefler.glucloser.models

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.RealmClass
import java.util.*

/**
 * Created by Nathan Lefler on 1/4/15.
 */
@RealmClass
public open class BloodSugar(
        public open var primaryId: String = UUID.randomUUID().toString(),
        public open var value: Int = 0,
        public open var date: Date? = null
    ) : RealmObject() {

    companion object {
        @Ignore
        public val ParseClassName: String = "BloodSugar"

        @Ignore
        public val IdFieldName: String = "primaryId"

        @Ignore
        public val ValueFieldName: String = "value"

        @Ignore
        public val DateFieldName: String = "date"
    }
}
