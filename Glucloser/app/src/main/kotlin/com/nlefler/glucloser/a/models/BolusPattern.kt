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
    var primaryId: String

    var updatedOn: Date

    @get:OneToMany(mappedBy = "pattern")
    var rates: MutableList<BolusRate>

}
