package com.nlefler.glucloser.a.models

/**
 * Created by Nathan Lefler on 12/29/14.
 */
data class CheckInPushedData (
    val venueId: String = "",
    val venueName: String = "",
    val venueLat: Float = 0.toFloat(),
    val venueLon: Float = 0.toFloat()
){}
