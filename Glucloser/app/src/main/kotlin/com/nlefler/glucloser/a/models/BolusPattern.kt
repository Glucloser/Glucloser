package com.nlefler.glucloser.a.models

import com.nlefler.glucloser.a.models.BolusRate
import io.requery.*
import java.util.*

/**
 * Created by nathan on 9/19/15.
 */
@Entity
interface BolusPattern: Persistable {
    @get:Key
    @get:Generated
    @get:io.requery.ForeignKey
    var primaryId: String
    var rates: MutableList<BolusRate>
}
