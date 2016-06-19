package com.nlefler.glucloser.a.models

import com.nlefler.glucloser.a.models.BolusRate
import io.requery.Entity
import io.requery.Key
import io.requery.OneToMany
import java.util.*

/**
 * Created by nathan on 9/19/15.
 */
@Entity
open class BolusPattern(
        @Key
        open val primaryId: String = UUID.randomUUID().toString(),
        @OneToMany
        open val rates: List<BolusRate>
    ) {
}
