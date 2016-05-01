package com.nlefler.glucloser.a.models

import java.util.*

/**
 * Created by nathan on 3/26/16.
 */
data class SensorReading(
        val primaryId: String = UUID.randomUUID().toString(),
        val timestamp: Date,
        val reading: Int): DBTable {
    override val tableName = SensorReading::class.simpleName!!
    override val primaryKeyField = SensorReading::primaryId.name
}
