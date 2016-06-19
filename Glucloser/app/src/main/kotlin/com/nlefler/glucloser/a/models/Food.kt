package com.nlefler.glucloser.a.models

import io.requery.Entity
import io.requery.Key
import java.util.*

/**
 * Created by Nathan Lefler on 5/16/15.
 */
@Entity
open class Food(
        @Key
        open val primaryId: String = UUID.randomUUID().toString(),
        open val carbs: Int,
        open val foodName: String
    ) {
}
