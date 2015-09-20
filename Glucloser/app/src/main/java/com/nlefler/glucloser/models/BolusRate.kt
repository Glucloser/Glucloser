package com.nlefler.glucloser.models

import io.realm.annotations.Ignore
import java.util.*

/**
 * Created by nathan on 8/30/15.
 */

public class BolusRate {
    val ordinal: Int? = null
    val rate: Int? = null
    val startTime: Int? = null

    companion object {
        val ParseClassName = "BolusRate"
    }
}
