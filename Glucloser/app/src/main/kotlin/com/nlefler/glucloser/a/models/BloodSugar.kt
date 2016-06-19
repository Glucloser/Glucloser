package com.nlefler.glucloser.a.models

import io.requery.Entity
import io.requery.Generated
import io.requery.Key
import io.requery.Persistable
import java.util.*

/**
 * Created by Nathan Lefler on 1/4/15.
 */
@Entity
open class BloodSugar (
        @Key
        open var primaryId: String = UUID.randomUUID().toString(),
        open val value: Int,
        open val recordedDate: Date
){
}
