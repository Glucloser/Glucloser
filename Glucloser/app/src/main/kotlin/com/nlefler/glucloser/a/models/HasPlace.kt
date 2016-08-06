package com.nlefler.glucloser.a.models

import io.requery.ManyToOne

/**
 * Created by Nathan Lefler on 5/16/15.
 */
interface HasPlace {
    @get:ManyToOne
    var place: Place?
}
