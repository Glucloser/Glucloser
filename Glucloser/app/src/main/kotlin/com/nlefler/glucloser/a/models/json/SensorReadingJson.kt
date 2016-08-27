package com.nlefler.glucloser.a.models.json

import java.util.*

/**
 * Created by nathan on 8/16/16.
 */
data class SensorReadingJson(val primaryId: String,
                             val recordedDate: Date,
                             val readingValue: Int) {
}
