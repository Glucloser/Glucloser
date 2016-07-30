package com.nlefler.glucloser.a.models

import io.requery.Entity
import io.requery.Generated
import io.requery.Key
import io.requery.Persistable
import java.util.*

/**
 * Created by Nathan Lefler on 5/16/15.
 */
@Entity
interface Food: Persistable {
    @get:Key
    @get:Generated
    @get:io.requery.ForeignKey
    var primaryID: String
    var carbs: Int
    var foodName: String
}
